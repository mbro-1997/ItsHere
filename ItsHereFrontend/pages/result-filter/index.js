Page({
  data: {
    keyword: "",
    activeTab: "全部",
    tabs: ["全部"],
    allOptions: [],
    options: []
  },

  onLoad() {
    this.buildOptions()
  },

  buildOptions() {
    const places = wx.getStorageSync("routePlaces") || []
    const config = wx.getStorageSync("resultFilterConfig") || { disabledBrandIds: [], disabledTags: [] }
    const brandMap = {}
    const tagMap = {}
    places.forEach(place => {
      const category = place.businessCategory || "其他"
      const brandKey = place.brandId || place.brandName || place.name
      if (!brandMap[brandKey]) {
        brandMap[brandKey] = {
          type: "brand",
          id: place.brandId,
          key: String(brandKey),
          category,
          mark: (place.brandName || place.name || "?").slice(0, 1),
          name: place.brandName || place.name,
          count: 0
        }
      }
      brandMap[brandKey].count += 1
      ;(place.tags || []).forEach(tag => {
        if (!tagMap[tag]) {
          tagMap[tag] = { type: "tag", id: tag, key: tag, category: "自定义标签", mark: tag.slice(0, 1), name: tag, count: 0 }
        }
        tagMap[tag].count += 1
      })
    })

    const options = [...Object.values(brandMap), ...Object.values(tagMap)].map(option => {
      const enabled = option.type === "tag"
        ? !(config.disabledTags || []).includes(option.name)
        : !(config.disabledBrandIds || []).includes(option.id)
      return {
        ...option,
        enabled,
        meta: `本次结果 ${option.count} 个点 · ${enabled ? "正在显示" : "已隐藏"}`
      }
    })
    const tabs = ["全部", ...new Set(options.map(item => item.category))]
    this.setData({ tabs, allOptions: options }, () => this.applyFilter())
  },

  onInput(event) {
    this.setData({ keyword: event.detail.value }, () => this.applyFilter())
  },

  onSelectTab(event) {
    this.setData({ activeTab: event.currentTarget.dataset.tab }, () => this.applyFilter())
  },

  applyFilter() {
    const keyword = this.data.keyword.trim()
    const options = this.data.allOptions.filter(option => {
      const matchTab = this.data.activeTab === "全部" || option.category === this.data.activeTab
      const matchKeyword = !keyword || option.name.includes(keyword)
      return matchTab && matchKeyword
    })
    this.setData({ options })
  },

  onToggle(event) {
    const { type, id, name } = event.currentTarget.dataset
    const enabled = event.detail.value
    const config = wx.getStorageSync("resultFilterConfig") || { disabledBrandIds: [], disabledTags: [] }
    if (type === "tag") {
      const next = new Set(config.disabledTags || [])
      enabled ? next.delete(name) : next.add(name)
      config.disabledTags = Array.from(next)
    } else {
      const numericId = Number(id)
      const next = new Set((config.disabledBrandIds || []).map(Number))
      enabled ? next.delete(numericId) : next.add(numericId)
      config.disabledBrandIds = Array.from(next)
    }
    wx.setStorageSync("resultFilterConfig", config)
    this.buildOptions()
  },

  onConfirm() {
    wx.navigateBack()
  }
})
