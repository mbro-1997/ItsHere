const POI_MARKER_ICON_MAP = {
  "快餐": "/assets/markers/poi-marker-food.png",
  "饮品": "/assets/markers/poi-marker-drink.png",
  "便利店": "/assets/markers/poi-marker-shop.png",
  "商超": "/assets/markers/poi-marker-shop.png",
  "生鲜菜场": "/assets/markers/poi-marker-shop.png",
  "药店": "/assets/markers/poi-marker-pharmacy.png",
  "公共设施": "/assets/markers/poi-marker-toilet.png"
}

const POI_MARKER_ICON_BY_CODE = {
  fast_food: "/assets/markers/poi-marker-food.png",
  drink: "/assets/markers/poi-marker-drink.png",
  convenience_store: "/assets/markers/poi-marker-shop.png",
  supermarket: "/assets/markers/poi-marker-shop.png",
  fresh_market: "/assets/markers/poi-marker-shop.png",
  pharmacy: "/assets/markers/poi-marker-pharmacy.png",
  public_facility: "/assets/markers/poi-marker-toilet.png"
}

const DEFAULT_POI_MARKER_ICON = "/assets/markers/poi-marker-default.png"

function getPoiMarkerIcon(categoryName, categoryCode) {
  return POI_MARKER_ICON_MAP[categoryName] || POI_MARKER_ICON_BY_CODE[categoryCode] || DEFAULT_POI_MARKER_ICON
}

module.exports = {
  getPoiMarkerIcon,
  DEFAULT_POI_MARKER_ICON
}
