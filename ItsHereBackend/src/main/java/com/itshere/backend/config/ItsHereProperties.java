package com.itshere.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "its-here")
public class ItsHereProperties {

    private final Wechat wechat = new Wechat();
    private final Map map = new Map();

    public Wechat getWechat() {
        return wechat;
    }

    public Map getMap() {
        return map;
    }

    public static class Wechat {
        private String appId;
        private String appSecret;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }
    }

    public static class Map {
        private String provider;
        private String key;
        private String routeUrl;
        private String geocoderUrl;
        private String suggestionUrl;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getRouteUrl() {
            return routeUrl;
        }

        public void setRouteUrl(String routeUrl) {
            this.routeUrl = routeUrl;
        }

        public String getGeocoderUrl() {
            return geocoderUrl;
        }

        public void setGeocoderUrl(String geocoderUrl) {
            this.geocoderUrl = geocoderUrl;
        }

        public String getSuggestionUrl() {
            return suggestionUrl;
        }

        public void setSuggestionUrl(String suggestionUrl) {
            this.suggestionUrl = suggestionUrl;
        }
    }
}
