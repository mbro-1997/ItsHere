package com.itshere.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UserPlaceSaveRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotNull
    private BigDecimal longitude;
    @NotNull
    private BigDecimal latitude;
    private Long categoryId;
    private String brandName;
    private String notes;
    private Boolean enabledForRoute = true;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getEnabledForRoute() {
        return enabledForRoute;
    }

    public void setEnabledForRoute(Boolean enabledForRoute) {
        this.enabledForRoute = enabledForRoute;
    }
}
