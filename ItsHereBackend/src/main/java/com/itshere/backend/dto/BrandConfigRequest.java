package com.itshere.backend.dto;

import jakarta.validation.constraints.NotNull;

public class BrandConfigRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long brandId;
    @NotNull
    private Boolean enabled;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
