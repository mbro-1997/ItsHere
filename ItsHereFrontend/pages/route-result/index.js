const app = getApp()
const { request } = require("../../utils/request")
const { getPoiMarkerIcon } = require("../../utils/poi-marker-icons")

Page({
  data: {
    loading: true,
    allPlaces: [],
    places: [],
    markers: [],
    polyline: [],
    selectedPlace: null,
    sheetExpanded: true,
    sheetDragging: false,
    sheetHeightRpx: 620,
    sheetExpandedHeightRpx: 620,
    sheetCollapsedHeightRpx: 176,
    sheetStartHeightRpx: 620,
    sheetTouchStartY: 0,
    filterButtonBottomRpx: 488,
    rpxPerPx: 2,
    center: { latitude: 36.651216, longitude: 117.119999 }
  },

  onLoad() {
    this.initSheetSize()
    this.loadRouteResult()
  },

  onShow() {
    this.syncRoutePlacesFromStorage()
    this.applyFrontendFilter()
  },

  syncRoutePlacesFromStorage() {
    const cachedPlaces = wx.getStorageSync("routePlaces") || []
    if (!cachedPlaces.length) {
      return
    }
    this.setData({ allPlaces: cachedPlaces.map(place => this.formatPlace(place)) })
  },

  async loadRouteResult() {
    const routeQuery = wx.getStorageSync("routeQuery") || {}
    const userId = await app.ensureLogin()
    if (!userId) {
      this.setData({ loading: false })
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    if (!routeQuery.start || !routeQuery.end) {
      this.setData({ loading: false })
      wx.showToast({ title: "请先选择路线", icon: "none" })
      return
    }
    try {
      const data = await request({
        url: "/api/routes/search",
        method: "POST",
        data: {
          userId,
          start: routeQuery.start,
          end: routeQuery.end,
          radiusMeters: routeQuery.radiusMeters || 500
        }
      })
      const allPlaces = (data.places || []).map(place => this.formatPlace(place))
      this.setRouteData(data.polyline || [], allPlaces)
    } catch (err) {
      this.setData({ loading: false, allPlaces: [], places: [], markers: [], polyline: [] })
      wx.showToast({ title: err.message || "顺路查询失败", icon: "none" })
    }
  },

  setRouteData(polylinePoints, allPlaces) {
    wx.setStorageSync("routePlaces", allPlaces)
    this.setData({
      loading: false,
      allPlaces,
      center: polylinePoints[0] || this.data.center,
      polyline: [{
        points: polylinePoints,
        color: "#0f9276",
        width: 6,
        dottedLine: false
      }]
    })
    this.applyFrontendFilter()
  },

  formatPlace(place) {
    const badges = []
    if (place.hasMyReview) {
      badges.push({ text: "有评价", type: "review" })
    }
    this.getTagBadges(place).forEach(tag => {
      badges.push({
        text: tag.name,
        type: tag.visibility === "public" ? "public-tag" : "private-tag"
      })
    })
    return {
      ...place,
      mark: (place.brandName || place.name || "?").slice(0, 1),
      meta: `${place.businessCategory || place.brandName || "点位"} · ${place.address || "暂无地址"}`,
      distanceText: place.distanceMeters == null ? "" : `${place.distanceMeters}m`,
      copyText: this.getCopyText(place),
      badges
    }
  },

  getTagBadges(place) {
    if (place.tagBadges && place.tagBadges.length) {
      return place.tagBadges
    }
    return (place.tags || []).map(name => ({ name, visibility: "private" }))
  },

  isToiletPlace(place) {
    return place && (
      place.businessCategory === "公共设施" ||
      place.brandName === "公共厕所" ||
      (place.name || "").includes("厕所")
    )
  },

  getCopyText(place) {
    if (this.isToiletPlace(place)) {
      return place.address || place.name || ""
    }
    return place.name || place.address || ""
  },

  applyFrontendFilter() {
    const config = wx.getStorageSync("resultFilterConfig") || { disabledBrandIds: [], disabledTags: [] }
    const disabledBrandIds = config.disabledBrandIds || []
    const disabledTags = config.disabledTags || []
    const places = (this.data.allPlaces || []).filter(place => {
      if (disabledBrandIds.includes(place.brandId)) {
        return false
      }
      const tags = place.tags || []
      if (tags.length > 0 && tags.every(tag => disabledTags.includes(tag))) {
        return false
      }
      return true
    })
    this.setData({
      places,
      markers: this.buildMarkers(places),
      selectedPlace: this.keepSelectedPlace(places)
    })
  },

  keepSelectedPlace(places) {
    const selected = this.data.selectedPlace
    if (!selected) {
      return null
    }
    return places.find(place => place.poiType === selected.poiType && place.id === selected.id) || null
  },

  buildMarkers(places) {
    return (places || []).map((place, index) => ({
      id: index + 1,
      latitude: place.latitude,
      longitude: place.longitude,
      title: place.name,
      iconPath: getPoiMarkerIcon(place.businessCategory, place.categoryCode),
      width: 42,
      height: 42,
      callout: {
        content: place.brandName || place.businessCategory || "",
        color: "#202828",
        fontSize: 11,
        borderRadius: 10,
        bgColor: "#ffffff",
        padding: 5,
        display: "BYCLICK"
      }
    }))
  },

  onOpenFilter() {
    wx.navigateTo({
      url: "/pages/result-filter/index",
      fail: (err) => {
        wx.showToast({ title: err.errMsg || "筛选页打开失败", icon: "none" })
      }
    })
  },

  initSheetSize() {
    wx.getSystemInfo({
      success: (res) => {
        const rpxPerPx = 750 / res.windowWidth
        const expandedHeightRpx = Math.round(res.windowHeight * 0.62 * rpxPerPx)
        const collapsedHeightRpx = 176
        this.setData({
          rpxPerPx,
          sheetExpandedHeightRpx: expandedHeightRpx,
          sheetCollapsedHeightRpx: collapsedHeightRpx,
          sheetHeightRpx: expandedHeightRpx,
          sheetStartHeightRpx: expandedHeightRpx,
          filterButtonBottomRpx: this.getFilterButtonBottom(expandedHeightRpx)
        })
      }
    })
  },

  onSheetTouchStart(event) {
    const touch = event.touches && event.touches[0]
    if (touch) {
      this.setData({
        sheetDragging: true,
        sheetTouchStartY: touch.clientY,
        sheetStartHeightRpx: this.data.sheetHeightRpx
      })
    }
  },

  onSheetTouchMove(event) {
    const touch = event.touches && event.touches[0]
    if (!touch) {
      return
    }
    const deltaPx = touch.clientY - this.data.sheetTouchStartY
    const nextHeight = this.clampSheetHeight(this.data.sheetStartHeightRpx - deltaPx * this.data.rpxPerPx)
    this.setData({
      sheetHeightRpx: nextHeight,
      filterButtonBottomRpx: this.getFilterButtonBottom(nextHeight)
    })
  },

  getFilterButtonBottom(heightRpx) {
    return Math.max(44, Math.round(heightRpx - 132))
  },

  clampSheetHeight(heightRpx) {
    return Math.max(
      this.data.sheetCollapsedHeightRpx,
      Math.min(this.data.sheetExpandedHeightRpx, Math.round(heightRpx))
    )
  },

  snapSheet(expanded) {
    const nextHeight = expanded ? this.data.sheetExpandedHeightRpx : this.data.sheetCollapsedHeightRpx
    this.setData({
      sheetExpanded: expanded,
      sheetDragging: false,
      sheetHeightRpx: nextHeight,
      filterButtonBottomRpx: this.getFilterButtonBottom(nextHeight)
    })
  },

  shouldExpandSheet() {
    const middle = (this.data.sheetExpandedHeightRpx + this.data.sheetCollapsedHeightRpx) / 2
    return this.data.sheetHeightRpx >= middle
  },

  onSheetTouchEnd(event) {
    const touch = event.changedTouches && event.changedTouches[0]
    if (!touch) {
      this.setData({ sheetDragging: false })
      return
    }
    const deltaY = touch.clientY - this.data.sheetTouchStartY
    if (Math.abs(deltaY) > 30) {
      this.snapSheet(deltaY < 0)
      return
    }
    this.snapSheet(this.shouldExpandSheet())
  },

  onToggleSheet() {
    this.snapSheet(!this.data.sheetExpanded)
  },

  onMarkerTap(event) {
    const markerId = event.detail.markerId
    const place = this.data.places[markerId - 1]
    if (!place) {
      return
    }
    this.setData({
      selectedPlace: place,
      center: {
        latitude: place.latitude,
        longitude: place.longitude
      }
    })
  },

  onClosePopup() {
    this.setData({ selectedPlace: null })
  },

  onOpenSelectedDetail() {
    const place = this.data.selectedPlace
    if (!place) {
      return
    }
    wx.navigateTo({ url: `/pages/place-detail/index?poiType=${place.poiType}&poiId=${place.id}` })
  },

  onOpenDetail(event) {
    const place = this.data.places[event.currentTarget.dataset.index]
    wx.navigateTo({ url: `/pages/place-detail/index?poiType=${place.poiType}&poiId=${place.id}` })
  },

  onCopyAddress(event) {
    const text = event.currentTarget.dataset.copy || ""
    wx.setClipboardData({ data: text })
  }
})
