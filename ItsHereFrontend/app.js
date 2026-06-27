const { showLoading, hideLoading } = require("./utils/loading")
const env = require("./utils/env")

App({
  globalData: {
    apiBaseUrl: env.apiBaseUrl,
    useDevLogin: env.useDevLogin,
    token: "",
    userId: null,
    nickname: "",
    loginReady: null
  },

  onLaunch() {
    this.globalData.loginReady = this.login()
  },

  login() {
    return new Promise((resolve) => {
      if (this.globalData.useDevLogin) {
        this.tryDevLogin()
          .then((userId) => resolve(userId))
          .catch(() => this.loginWithWechat(resolve))
        return
      }
      this.loginWithWechat(resolve)
    })
  },

  loginWithWechat(resolve) {
      wx.login({
        success: ({ code }) => {
          if (!code) {
            this.handleLoginFailure(resolve)
            return
          }
          showLoading()
          wx.request({
            url: `${this.globalData.apiBaseUrl}/api/auth/login`,
            method: "POST",
            data: { code },
            success: (res) => {
              const body = res.data || {}
              if (body.code === 0 && body.data) {
                this.saveLogin(body.data)
                resolve(body.data.userId)
                return
              }
              this.handleLoginFailure(resolve)
            },
            fail: () => this.handleLoginFailure(resolve),
            complete: () => hideLoading()
          })
        },
        fail: () => this.handleLoginFailure(resolve)
      })
  },

  ensureLogin() {
    if (this.globalData.userId) {
      return Promise.resolve(this.globalData.userId)
    }
    if (this.globalData.loginReady) {
      return this.globalData.loginReady
    }
    this.globalData.loginReady = this.login()
    return this.globalData.loginReady
  },

  handleLoginFailure(resolve) {
    this.tryDevLogin()
      .then((userId) => resolve(userId))
      .catch(() => {
        const cached = wx.getStorageSync("userId")
        const userId = cached || null
        this.globalData.userId = userId
        this.globalData.token = userId ? String(userId) : ""
        if (!userId) {
          wx.showToast({ title: `登录失败，请检查 ${this.globalData.apiBaseUrl}`, icon: "none" })
        }
        resolve(userId)
      })
  },

  tryDevLogin() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: `${this.globalData.apiBaseUrl}/api/auth/dev-login`,
        method: "POST",
        data: {},
        success: (res) => {
          const body = res.data || {}
          if (body.code === 0 && body.data) {
            this.saveLogin(body.data)
            resolve(body.data.userId)
            return
          }
          reject(new Error(body.message || "开发登录失败"))
        },
        fail: reject
      })
    })
  },

  saveLogin(data) {
    this.globalData.userId = data.userId
    this.globalData.token = data.token || String(data.userId || "")
    this.globalData.nickname = data.nickname || "济南微信用户"
    if (data.userId) {
      wx.setStorageSync("userId", data.userId)
    }
    wx.setStorageSync("nickname", this.globalData.nickname)
  }
})
