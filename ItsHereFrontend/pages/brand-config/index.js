const app = getApp()
const { request } = require("../../utils/request")

Page({
  data: {
    brands: [],
    visibleBrands: [],
    categories: ["全部"],
    activeCategory: "全部",
    loginMissing: false,
    loadFailed: false
  },

  onShow() {
    this.loadBrands()
  },

  async loadBrands() {
    const userId = await app.ensureLogin()
    if (!userId) {
      this.setData({ brands: [], visibleBrands: [], categories: ["全部"], loginMissing: true, loadFailed: false })
      return
    }
    try {
      const brands = await request({ url: `/api/brands?userId=${userId}` })
      const normalizedBrands = (brands || []).map(item => ({
        ...item,
        mark: (item.name || "?").slice(0, 1),
        meta: `${item.categoryName || "未分类"} · ${item.accountEnabled ? "参与顺路查询" : "未启用"}`,
        enabled: !!item.accountEnabled
      }))
      const categories = ["全部"].concat(
        normalizedBrands
          .map(item => item.categoryName || "未分类")
          .filter((item, index, list) => list.indexOf(item) === index)
      )
      const activeCategory = categories.includes(this.data.activeCategory) ? this.data.activeCategory : "全部"
      this.setData({
        loginMissing: false,
        loadFailed: false,
        brands: normalizedBrands,
        categories,
        activeCategory,
        visibleBrands: this.filterBrands(normalizedBrands, activeCategory)
      })
    } catch (err) {
      wx.showToast({ title: err.message || "品牌加载失败", icon: "none" })
      this.setData({ brands: [], visibleBrands: [], categories: ["全部"], loginMissing: false, loadFailed: true })
    }
  },

  filterBrands(brands, category) {
    if (category === "全部") {
      return brands
    }
    return brands.filter(item => (item.categoryName || "未分类") === category)
  },

  onSelectCategory(event) {
    const category = event.currentTarget.dataset.category
    this.setData({
      activeCategory: category,
      visibleBrands: this.filterBrands(this.data.brands, category)
    })
  },

  async onToggle(event) {
    const userId = await app.ensureLogin()
    if (!userId) {
      wx.showToast({ title: "请先完成登录", icon: "none" })
      return
    }
    const brandId = Number(event.currentTarget.dataset.id)
    const enabled = event.detail.value
    try {
      await request({
        url: "/api/brands/config",
        method: "POST",
        data: { userId, brandId, enabled }
      })
      this.loadBrands()
    } catch (err) {
      wx.showToast({ title: err.message || "保存失败", icon: "none" })
    }
  },

})
