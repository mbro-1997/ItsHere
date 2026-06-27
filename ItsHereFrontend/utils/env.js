let localEnv = {}

try {
  localEnv = require("./env.local")
} catch (err) {
  localEnv = {}
}

const env = {
  apiBaseUrl: "",
  useDevLogin: false,
  ...localEnv
}

module.exports = env
