const { request } = require("../../utils/request")

let suggestTimer = null
const suggestionCache = {}

Page({
  data: {
    target: "start",
    keyword: "",
    selectionStart: 0,
    selectionEnd: 0,
    suggestions: [],
    searching: false
  },

  onLoad(options) {
    const target = options.target || "start"
    const keyword = decodeURIComponent(options.keyword || "")
    this.setData({
      target,
      keyword,
      selectionStart: 0,
      selectionEnd: keyword.length
    })
    if (keyword.trim().length >= 2) {
      this.debounceSuggestion(keyword)
    }
  },

  onUnload() {
    if (suggestTimer) {
      clearTimeout(suggestTimer)
      suggestTimer = null
    }
  },

  onInput(event) {
    const keyword = event.detail.value
    this.setData({
      keyword,
      selectionStart: keyword.length,
      selectionEnd: keyword.length
    })
    this.debounceSuggestion(keyword)
  },

  onConfirm() {
    if (this.data.suggestions.length > 0) {
      this.selectSuggestion(this.data.suggestions[0])
      return
    }
    wx.showToast({ title: "请从下方选择地点", icon: "none" })
  },

  debounceSuggestion(keyword) {
    if (suggestTimer) {
      clearTimeout(suggestTimer)
    }
    const normalized = (keyword || "").trim()
    if (normalized.length < 2) {
      this.setData({ suggestions: [], searching: false })
      return
    }
    this.setData({ searching: true })
    suggestTimer = setTimeout(() => {
      this.loadSuggestions(normalized)
    }, 900)
  },

  async loadSuggestions(keyword) {
    if (suggestionCache[keyword]) {
      this.setData({ suggestions: suggestionCache[keyword], searching: false })
      return
    }
    try {
      const suggestions = await request({
        url: `/api/map/suggestion?keyword=${encodeURIComponent(keyword)}&region=${encodeURIComponent("济南市")}`,
        loading: false
      })
      suggestionCache[keyword] = suggestions || []
      this.setData({ suggestions: suggestionCache[keyword], searching: false })
    } catch (err) {
      this.setData({ suggestions: [], searching: false })
      wx.showToast({ title: err.message || "地点联想失败", icon: "none" })
    }
  },

  onSelectSuggestion(event) {
    const suggestion = this.data.suggestions[event.currentTarget.dataset.index]
    if (suggestion) {
      this.selectSuggestion(suggestion)
    }
  },

  selectSuggestion(suggestion) {
    const location = {
      latitude: suggestion.latitude,
      longitude: suggestion.longitude,
      name: suggestion.title || "地点",
      address: suggestion.address || ""
    }
    const pages = getCurrentPages()
    const previous = pages[pages.length - 2]
    if (previous && typeof previous.selectUserPlaceLocation === "function") {
      previous.selectUserPlaceLocation(location)
    } else if (previous && typeof previous.selectRoutePoint === "function") {
      previous.selectRoutePoint(this.data.target, location)
    }
    wx.navigateBack()
  }
})
