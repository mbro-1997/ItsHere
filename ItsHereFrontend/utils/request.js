const app = getApp()
const { showLoading, hideLoading } = require("./loading")

function request(options) {
  const baseUrl = app.globalData.apiBaseUrl || ""
  if (options.loading !== false) {
    showLoading()
  }
  return new Promise((resolve, reject) => {
    wx.request({
      url: baseUrl + options.url,
      method: options.method || "GET",
      data: options.data || {},
      header: {
        "content-type": "application/json",
        ...(options.header || {})
      },
      success(res) {
        const body = res.data || {}
        if (body.code === 0) {
          resolve(body.data)
          return
        }
        reject(new Error(body.message || "请求失败"))
      },
      fail(err) {
        reject(err)
      },
      complete() {
        if (options.loading !== false) {
          hideLoading()
        }
      }
    })
  })
}

module.exports = {
  request
}
