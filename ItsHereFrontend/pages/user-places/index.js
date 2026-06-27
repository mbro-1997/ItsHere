const app = getApp()
const { request } = require("../../utils/request")

Page({
  data: {
    places: [],
    selectedPlace: null,
    touchStartX: 0,
    touchStartTranslateX: 0,
    swipeIndex: -1
  },

  onLoad() {
    this.loadPlaces()
  },

  async loadPlaces() {
    const userId = await app.ensureLogin()
    if (!userId) {
      this.setData({ places: [] })
      return
    }
    try {
      const places = await request({ url: `/api/user-places?userId=${userId}` })
      this.setData({
        places: (places || []).map(item => ({
          ...item,
          addressText: item.address || "暂无地址",
          translateX: 0,
          swiping: false
        }))
      })
    } catch (err) {
      wx.showToast({ title: err.message || "个人点位加载失败", icon: "none" })
      this.setData({ places: [] })
    }
  },

  onShowForm() {
    if (this.data.places.length >= 5) {
      wx.showToast({ title: "最多添加 5 个常用地点", icon: "none" })
      return
    }
    wx.navigateTo({
      url: "/pages/location-search/index?target=userPlace"
    })
  },

  selectUserPlaceLocation(location) {
    this.setData({
      selectedPlace: {
        name: location.name || "地点",
        address: location.address || "暂无详细地址",
        latitude: location.latitude,
        longitude: location.longitude
      }
    })
  },

  onClearSelected() {
    this.setData({ selectedPlace: null })
  },

  onPlaceTouchStart(event) {
    const touch = event.touches && event.touches[0]
    const index = Number(event.currentTarget.dataset.index)
    if (touch) {
      this.setData({
        touchStartX: touch.clientX,
        touchStartTranslateX: this.data.places[index] ? this.data.places[index].translateX || 0 : 0,
        swipeIndex: index,
        places: this.data.places.map((item, itemIndex) => ({
          ...item,
          swiping: itemIndex === index,
          translateX: itemIndex === index ? (item.translateX || 0) : 0
        }))
      })
    }
  },

  onPlaceTouchMove(event) {
    const touch = event.touches && event.touches[0]
    const index = Number(event.currentTarget.dataset.index)
    if (!touch || index !== this.data.swipeIndex) {
      return
    }
    const deltaX = touch.clientX - this.data.touchStartX
    const nextX = this.clampSwipeX(this.data.touchStartTranslateX + deltaX)
    this.setData({
      places: this.data.places.map((item, itemIndex) => ({
        ...item,
        translateX: itemIndex === index ? nextX : item.translateX
      }))
    })
  },

  clampSwipeX(value) {
    return Math.max(-150, Math.min(0, Math.round(value)))
  },

  onPlaceTouchEnd(event) {
    const touch = event.changedTouches && event.changedTouches[0]
    if (!touch) {
      return
    }
    const deltaX = touch.clientX - this.data.touchStartX
    const index = Number(event.currentTarget.dataset.index)
    const current = this.data.places[index] ? this.data.places[index].translateX || 0 : 0
    const shouldOpen = deltaX < -30 || current < -75
    const places = this.data.places.map((item, itemIndex) => ({
      ...item,
      swiping: false,
      translateX: itemIndex === index && shouldOpen ? -150 : 0
    }))
    this.setData({ places, swipeIndex: -1 })
  },

  onCloseDelete() {
    this.setData({
      places: this.data.places.map(item => ({ ...item, translateX: 0, swiping: false }))
    })
  },

  async onDeletePlace(event) {
    const id = event.currentTarget.dataset.id
    const userId = await app.ensureLogin()
    if (!userId || !id) {
      return
    }
    wx.showModal({
      title: "删除地点",
      content: "确认删除这个个人点位吗？",
      confirmText: "删除",
      confirmColor: "#d64545",
      success: async (res) => {
        if (!res.confirm) {
          return
        }
        try {
          await request({
            url: `/api/user-places/${id}?userId=${userId}`,
            method: "DELETE"
          })
          this.loadPlaces()
        } catch (err) {
          wx.showToast({ title: err.message || "删除失败", icon: "none" })
        }
      }
    })
  },

  async onSubmit() {
    const userId = await app.ensureLogin()
    if (!userId) {
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    const place = this.data.selectedPlace
    if (!place || !place.name || !place.latitude || !place.longitude) {
      wx.showToast({ title: "请先搜索并选择地点", icon: "none" })
      return
    }
    try {
      await request({
        url: "/api/user-places",
        method: "POST",
        data: {
          userId,
          name: place.name,
          address: place.address,
          latitude: Number(place.latitude),
          longitude: Number(place.longitude),
          notes: "",
          enabledForRoute: true
        }
      })
      this.setData({ selectedPlace: null })
      this.loadPlaces()
    } catch (err) {
      wx.showToast({ title: err.message || "保存失败", icon: "none" })
    }
  }
})
