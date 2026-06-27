const app = getApp()

Page({
  data: {
    nickname: "济南微信用户"
  },

  onShow() {
    app.ensureLogin().then(() => {
      this.setData({
        nickname: app.globalData.nickname || wx.getStorageSync("nickname") || "济南微信用户"
      })
    })
  },

  goBrandConfig() { wx.navigateTo({ url: "/pages/brand-config/index" }) },
  goBrandSuggestion() { wx.navigateTo({ url: "/pages/brand-suggestion/index" }) },
  goUserPlaces() { wx.navigateTo({ url: "/pages/user-places/index" }) },
  goReviews() { wx.navigateTo({ url: "/pages/reviews/index" }) }
})
