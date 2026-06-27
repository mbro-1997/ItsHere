package com.itshere.backend.dto;

import jakarta.validation.constraints.NotNull;

public class CoordinateDto {
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    private String name;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
