let loadingCount = 0
let hideTimer = null

function showLoading() {
  loadingCount += 1
  if (hideTimer) {
    clearTimeout(hideTimer)
    hideTimer = null
  }
  updatePages(true)
}

function hideLoading() {
  loadingCount = Math.max(0, loadingCount - 1)
  if (loadingCount > 0) {
    return
  }
  hideTimer = setTimeout(() => {
    updatePages(false)
  }, 120)
}

function updatePages(visible) {
  const pages = getCurrentPages()
  if (!pages || pages.length === 0) {
    return
  }
  const current = pages[pages.length - 1]
  if (current && typeof current.setData === "function") {
    current.setData({ globalLoading: visible })
  }
}

module.exports = {
  showLoading,
  hideLoading
}
