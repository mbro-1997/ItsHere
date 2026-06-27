const app = getApp()
const { request } = require("../../utils/request")

Page({
  data: {
    brandName: "",
    result: null
  },

  onInput(event) {
    this.setData({ brandName: event.detail.value })
  },

  async onSubmit() {
    const brandName = this.data.brandName.trim()
    if (!brandName) {
      wx.showToast({ title: "请输入品牌名称", icon: "none" })
      return
    }
    const userId = await app.ensureLogin()
    if (!userId) {
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    try {
      const result = await request({
        url: "/api/brand-suggestions",
        method: "POST",
        data: {
          userId,
          brandName
        }
      })
      const viewResult = this.formatResult(result)
      this.setData({ result: viewResult, brandName: "" })
      wx.showToast({ title: result.duplicate ? "已建议过" : "已提交", icon: "success" })
    } catch (err) {
      wx.showToast({ title: err.message || "提交失败", icon: "none" })
    }
  },

  formatResult(result) {
    const count = Math.min(result.suggestUserCount || 0, 10)
    const reached = result.status === "reached" || result.status === "imported"
    let message = result.duplicate
      ? "你已经建议过这个品牌，系统不会重复计数。"
      : "你的建议已经收到。"
    if (reached) {
      message = result.status === "imported"
        ? "这个品牌已经进入品牌配置。"
        : "这个品牌已经达到建议人数，等待补充点位数据。"
    }
    return {
      ...result,
      progress: Math.round(count * 10),
      message
    }
  }
})
