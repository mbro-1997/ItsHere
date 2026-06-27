package com.itshere.backend.dto;

public class BrandSuggestionResponse {
    private String rawName;
    private String normalizedName;
    private Integer suggestUserCount;
    private String status;
    private Boolean duplicate;

    public String getRawName() {
        return rawName;
    }

    public void setRawName(String rawName) {
        this.rawName = rawName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public Integer getSuggestUserCount() {
        return suggestUserCount;
    }

    public void setSuggestUserCount(Integer suggestUserCount) {
        this.suggestUserCount = suggestUserCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }
}
