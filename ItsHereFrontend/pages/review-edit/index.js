const app = getApp()
const { request } = require("../../utils/request")

Page({
  data: {
    poiType: "system",
    poiId: null,
    rating: 5,
    visibility: "private",
    content: ""
  },

  onLoad(options) {
    this.setData({
      poiType: options.poiType || "system",
      poiId: options.poiId || null
    })
  },

  onInput(event) {
    this.setData({ content: event.detail.value })
  },

  onRating(event) {
    this.setData({ rating: Number(event.currentTarget.dataset.rating) })
  },

  onVisibility(event) {
    this.setData({ visibility: event.currentTarget.dataset.visibility })
  },

  async onSubmit() {
    const userId = await app.ensureLogin()
    if (!userId) {
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    if (!this.data.poiId || !this.data.content.trim()) {
      wx.showToast({ title: "请填写评价内容", icon: "none" })
      return
    }
    try {
      await request({
        url: "/api/reviews",
        method: "POST",
        data: {
          userId,
          poiType: this.data.poiType,
          poiId: Number(this.data.poiId),
          rating: this.data.rating,
          content: this.data.content,
          visibility: this.data.visibility
        }
      })
      wx.showToast({ title: "已保存", icon: "success" })
      wx.navigateBack()
    } catch (err) {
      wx.showToast({ title: err.message || "保存失败", icon: "none" })
    }
  }
})
