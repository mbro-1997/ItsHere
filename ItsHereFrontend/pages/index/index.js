const app = getApp()
const { request } = require("../../utils/request")
const { DEFAULT_POI_MARKER_ICON } = require("../../utils/poi-marker-icons")

Page({
  data: {
    startText: "当前位置",
    endText: "选择终点",
    start: { latitude: 36.651216, longitude: 117.119999, name: "当前位置" },
    currentLocation: null,
    mapCenter: { latitude: 36.651216, longitude: 117.119999 },
    startManuallySelected: false,
    end: null,
    radiusMeters: 500,
    markers: [
      { id: 1, latitude: 36.651216, longitude: 117.119999, title: "起点", iconPath: DEFAULT_POI_MARKER_ICON, width: 42, height: 42 }
    ]
  },

  onLoad() {
    app.ensureLogin()
    this.loadCurrentLocation()
  },

  loadCurrentLocation(forceStart) {
    wx.getLocation({
      type: "gcj02",
      success: async (res) => {
        const start = {
          latitude: res.latitude,
          longitude: res.longitude,
          name: "当前位置"
        }
        const nextData = {
          currentLocation: start,
          mapCenter: start
        }
        if (forceStart || !this.data.startManuallySelected) {
          nextData.start = start
          nextData.startText = "当前位置"
          nextData.startManuallySelected = false
          nextData.markers = this.buildMarkers(start, this.data.end)
        }
        this.setData(nextData)
        try {
          const locationName = await request({
            url: `/api/map/reverse-geocode?latitude=${res.latitude}&longitude=${res.longitude}`,
            loading: false
          })
          const resolvedName = locationName || "当前位置"
          const resolvedStart = {
            ...start,
            name: resolvedName,
            address: resolvedName
          }
          const resolvedData = {
            currentLocation: resolvedStart
          }
          if (forceStart || !this.data.startManuallySelected) {
            resolvedData.start = resolvedStart
            resolvedData.startText = resolvedName
            resolvedData.startManuallySelected = false
            resolvedData.markers = this.buildMarkers(resolvedStart, this.data.end)
          }
          this.setData(resolvedData)
        } catch (err) {
          if (!this.data.startManuallySelected) {
            this.setData({ startText: "当前位置" })
          }
        }
      },
      fail: () => {
        wx.showToast({ title: "定位失败，可手动选择起点", icon: "none" })
      }
    })
  },

  onOpenStartSearch() {
    wx.navigateTo({
      url: `/pages/location-search/index?target=start&keyword=${encodeURIComponent(this.data.startText || "")}`
    })
  },

  onOpenEndSearch() {
    wx.navigateTo({
      url: `/pages/location-search/index?target=end&keyword=${encodeURIComponent(this.data.end ? this.data.endText : "")}`
    })
  },

  selectRoutePoint(target, location) {
    if (target === "start") {
      this.setData({
        start: location,
        startText: location.name,
        startManuallySelected: true,
        mapCenter: {
          latitude: location.latitude,
          longitude: location.longitude
        },
        markers: this.buildMarkers(location, this.data.end)
      })
      return
    }
    this.setData({
      end: location,
      endText: location.name,
      mapCenter: {
        latitude: location.latitude,
        longitude: location.longitude
      },
      markers: this.buildMarkers(this.data.start, location)
    })
  },

  onLocateMe() {
    this.loadCurrentLocation(true)
  },

  buildMarkers(start, end) {
    const markers = []
    if (start) {
      markers.push({ id: 1, latitude: start.latitude, longitude: start.longitude, title: "起点", iconPath: DEFAULT_POI_MARKER_ICON, width: 42, height: 42 })
    }
    if (end) {
      markers.push({ id: 2, latitude: end.latitude, longitude: end.longitude, title: "终点", iconPath: DEFAULT_POI_MARKER_ICON, width: 42, height: 42 })
    }
    return markers
  },

  onSearchRoute() {
    if (!this.data.start || !this.data.end) {
      wx.showToast({ title: "请先选择起点和终点", icon: "none" })
      return
    }
    wx.setStorageSync("routeQuery", {
      start: this.data.start,
      end: this.data.end,
      radiusMeters: this.data.radiusMeters
    })
    wx.navigateTo({ url: "/pages/route-result/index" })
  }
})
