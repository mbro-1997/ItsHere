const app = getApp()
const { request } = require("../../utils/request")

Page({
  data: {
    reviews: []
  },

  onLoad() {
    this.loadReviews()
  },

  async loadReviews() {
    const userId = await app.ensureLogin()
    if (!userId) {
      this.setData({ reviews: [] })
      return
    }
    try {
      const reviews = await request({ url: `/api/reviews/mine?userId=${userId}` })
      this.setData({
        reviews: (reviews || []).map(item => this.formatReview(item))
      })
    } catch (err) {
      wx.showToast({ title: err.message || "评价加载失败", icon: "none" })
      this.setData({ reviews: [] })
    }
  },

  formatReview(review) {
    const isPrivate = review.visibility !== "public"
    return {
      ...review,
      name: `${review.poiType === "user" ? "个人点位" : "系统点位"} #${review.poiId}`,
      text: review.content,
      stars: "★★★★★".slice(0, review.rating || 5),
      visibilityText: isPrivate ? "个人评价" : "公开评价",
      timeText: this.formatTime(review.updatedAt || review.createdAt)
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
  }
})
