const app = getApp()
const { request } = require("../../utils/request")
const { DEFAULT_POI_MARKER_ICON } = require("../../utils/poi-marker-icons")

Page({
  data: {
    poiType: "system",
    poiId: null,
    place: null,
    reviews: [],
    tags: [],
    tagBadges: [],
    markers: []
  },

  onLoad(options) {
    this.setData({
      poiType: options.poiType || "system",
      poiId: options.poiId || null
    })
    this.loadDetail()
  },

  onShow() {
    if (this.data.poiId) {
      this.loadDetail()
    }
  },

  async loadDetail() {
    const userId = await app.ensureLogin()
    if (!userId) {
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    if (!this.data.poiId) {
      wx.showToast({ title: "缺少点位 ID", icon: "none" })
      return
    }
    try {
      const detail = await request({
        url: `/api/places/${this.data.poiType}/${this.data.poiId}?userId=${userId}`
      })
      this.setData({
        place: detail.place,
        reviews: (detail.reviews || []).map(item => this.formatReview(item)),
        tags: detail.tags || [],
        tagBadges: this.formatTagBadges(detail)
      }, () => this.refreshMarkers())
    } catch (err) {
      wx.showToast({ title: err.message || "详情加载失败", icon: "none" })
      this.refreshMarkers()
    }
  },

  formatTagBadges(detail) {
    if (detail.tagBadges && detail.tagBadges.length) {
      return detail.tagBadges
    }
    return (detail.tags || []).map(name => ({ name, visibility: "private" }))
  },

  refreshMarkers() {
    const place = this.data.place || {}
    if (!place.latitude || !place.longitude) {
      this.setData({ markers: [] })
      return
    }
    this.setData({
      markers: [{
        id: 1,
        latitude: place.latitude,
        longitude: place.longitude,
        title: place.name,
        iconPath: DEFAULT_POI_MARKER_ICON,
        width: 42,
        height: 42
      }]
    })
  },

  onCopyAddress() {
    wx.setClipboardData({ data: this.getCopyText(this.data.place) })
  },

  isToiletPlace(place) {
    return place && (
      place.businessCategory === "公共设施" ||
      place.brandName === "公共厕所" ||
      (place.name || "").includes("厕所")
    )
  },

  getCopyText(place) {
    if (!place) {
      return ""
    }
    if (this.isToiletPlace(place)) {
      return place.address || place.name || ""
    }
    return place.name || place.address || ""
  },

  formatReview(review) {
    const isPrivate = review.visibility !== "public"
    return {
      ...review,
      visibilityText: isPrivate ? "个人评价" : "公开评价",
      reviewerText: isPrivate ? "仅自己可见" : (review.userNickname || "微信用户"),
      timeText: this.formatTime(review.updatedAt || review.createdAt),
      stars: "★★★★★".slice(0, review.rating || 5)
    }
  },

  formatTime(value) {
    if (!value) {
      return ""
    }
    if (Array.isArray(value)) {
      const [year, month, day, hour = 0, minute = 0] = value
      return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")} ${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`
    }
    return String(value).replace("T", " ").slice(0, 16)
  },

  onWriteReview() {
    wx.navigateTo({ url: `/pages/review-edit/index?poiType=${this.data.poiType}&poiId=${this.data.poiId || ""}` })
  },

  onEditTags() {
    wx.navigateTo({ url: `/pages/tag-edit/index?poiType=${this.data.poiType}&poiId=${this.data.poiId || ""}` })
  }
})
