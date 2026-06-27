package com.itshere.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class PlaceSummaryDto {
    private Long id;
    private String poiType = "system";
    private String name;
    private String brandName;
    private Long brandId;
    private String businessCategory;
    private Long categoryId;
    private String address;
    private String phone;
    private Double longitude;
    private Double latitude;
    private Integer distanceMeters;
    private Boolean hasMyReview = false;
    private List<String> tags = new ArrayList<>();
    private List<TagBadgeDto> tagBadges = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoiType() {
        return poiType;
    }

    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Integer distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Boolean getHasMyReview() {
        return hasMyReview;
    }

    public void setHasMyReview(Boolean hasMyReview) {
        this.hasMyReview = hasMyReview;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<TagBadgeDto> getTagBadges() {
        return tagBadges;
    }

    public void setTagBadges(List<TagBadgeDto> tagBadges) {
        this.tagBadges = tagBadges;
    }
}
