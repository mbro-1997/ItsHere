const app = getApp()
const { request } = require("../../utils/request")

Page({
  data: {
    poiType: "system",
    poiId: null,
    tags: [],
    inputValue: "",
    touchStartX: 0,
    touchStartTranslateX: 0,
    swipeIndex: -1
  },

  onLoad(options) {
    this.setData({
      poiType: options.poiType || "system",
      poiId: options.poiId || null
    })
    this.loadExistingTags()
  },

  async loadExistingTags() {
    const userId = await app.ensureLogin()
    if (!userId) {
      return
    }
    if (!this.data.poiId) {
      return
    }
    try {
      const tags = await request({
        url: `/api/tags/poi?userId=${userId}&poiType=${this.data.poiType}&poiId=${Number(this.data.poiId)}`
      })
      const tagNames = (tags || []).map(item => item.name)
      this.setData({ tags: this.formatTags(tagNames) })
    } catch (err) {
      this.setData({ tags: [] })
    }
  },

  onInput(event) {
    this.setData({ inputValue: event.detail.value })
  },

  async onAddTag() {
    const userId = await app.ensureLogin()
    if (!userId) {
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    const tagName = this.data.inputValue.trim()
    if (!this.data.poiId || !tagName) {
      wx.showToast({ title: "请输入标签", icon: "none" })
      return
    }
    try {
      const tags = await request({
        url: "/api/tags",
        method: "POST",
        data: {
          userId,
          poiType: this.data.poiType,
          poiId: Number(this.data.poiId),
          tagName
        }
      })
      const tagNames = (tags || []).map(item => item.name)
      this.setData({ tags: this.formatTags(tagNames), inputValue: "" })
      await this.syncVisibleRoutePlaceTags(userId)
      wx.showToast({ title: "已添加", icon: "success" })
    } catch (err) {
      wx.showToast({ title: err.message || "保存失败", icon: "none" })
    }
  },

  async onDeleteTag(event) {
    const tagName = event.currentTarget.dataset.name
    const userId = await app.ensureLogin()
    if (!userId || !this.data.poiId || !tagName) {
      return
    }
    try {
      const tags = await request({
        url: `/api/tags?userId=${userId}&poiType=${this.data.poiType}&poiId=${Number(this.data.poiId)}&tagName=${encodeURIComponent(tagName)}`,
        method: "DELETE"
      })
      const tagNames = (tags || []).map(item => item.name)
      this.setData({ tags: this.formatTags(tagNames), swipeIndex: -1 })
      await this.syncVisibleRoutePlaceTags(userId)
      wx.showToast({ title: "已删除", icon: "success" })
    } catch (err) {
      wx.showToast({ title: err.message || "删除失败", icon: "none" })
    }
  },

  onTagTouchStart(event) {
    const touch = event.touches && event.touches[0]
    const index = Number(event.currentTarget.dataset.index)
    if (!touch) {
      return
    }
    this.setData({
      touchStartX: touch.clientX,
      touchStartTranslateX: this.data.tags[index] ? this.data.tags[index].translateX || 0 : 0,
      swipeIndex: index,
      tags: this.data.tags.map((item, itemIndex) => ({
        ...item,
        swiping: itemIndex === index,
        translateX: itemIndex === index ? (item.translateX || 0) : 0
      }))
    })
  },

  onTagTouchMove(event) {
    const touch = event.touches && event.touches[0]
    const index = Number(event.currentTarget.dataset.index)
    if (!touch || index !== this.data.swipeIndex) {
      return
    }
    const deltaX = touch.clientX - this.data.touchStartX
    const nextX = this.clampSwipeX(this.data.touchStartTranslateX + deltaX)
    this.setData({
      tags: this.data.tags.map((item, itemIndex) => ({
        ...item,
        translateX: itemIndex === index ? nextX : item.translateX
      }))
    })
  },

  onTagTouchEnd(event) {
    const touch = event.changedTouches && event.changedTouches[0]
    if (!touch) {
      return
    }
    const deltaX = touch.clientX - this.data.touchStartX
    const index = Number(event.currentTarget.dataset.index)
    const current = this.data.tags[index] ? this.data.tags[index].translateX || 0 : 0
    const shouldOpen = deltaX < -30 || current < -75
    this.setData({
      tags: this.data.tags.map((item, itemIndex) => ({
        ...item,
        swiping: false,
        translateX: itemIndex === index && shouldOpen ? -150 : 0
      })),
      swipeIndex: -1
    })
  },

  onCloseDelete() {
    this.setData({
      tags: this.data.tags.map(item => ({ ...item, translateX: 0, swiping: false }))
    })
  },

  clampSwipeX(value) {
    return Math.max(-150, Math.min(0, Math.round(value)))
  },

  formatTags(tagNames) {
    return (tagNames || []).map(name => ({
      name,
      mark: String(name || "标").slice(0, 1),
      translateX: 0,
      swiping: false
    }))
  },

  async syncVisibleRoutePlaceTags(userId) {
    try {
      const detail = await request({
        url: `/api/places/${this.data.poiType}/${this.data.poiId}?userId=${userId}`
      })
      this.syncRoutePlaceTags(detail.tags || [], detail.tagBadges || [])
    } catch (err) {
      const tagNames = this.data.tags.map(item => item.name)
      this.syncRoutePlaceTags(tagNames, [])
    }
  },

  syncRoutePlaceTags(tagNames, tagBadges) {
    const poiId = Number(this.data.poiId)
    const poiType = this.data.poiType
    const places = wx.getStorageSync("routePlaces") || []
    let changed = false
    const nextPlaces = places.map(place => {
      if (place.poiType === poiType && Number(place.id) === poiId) {
        changed = true
        return {
          ...place,
          tags: tagNames,
          tagBadges,
          badges: this.buildBadges(place, tagNames, tagBadges)
        }
      }
      return place
    })
    if (changed) {
      wx.setStorageSync("routePlaces", nextPlaces)
      wx.setStorageSync("routePlacesVersion", Date.now())
    }
  },

  buildBadges(place, tagNames, tagBadges) {
    const badges = []
    if (place.hasMyReview) {
      badges.push({ text: "有评价", type: "review" })
    }
    const displayTagBadges = tagBadges && tagBadges.length
      ? tagBadges
      : tagNames.map(name => ({ name, visibility: "private" }))
    displayTagBadges.forEach(tag => {
      badges.push({
        text: tag.name,
        type: tag.visibility === "public" ? "public-tag" : "private-tag"
      })
    })
    return badges
  }
})
