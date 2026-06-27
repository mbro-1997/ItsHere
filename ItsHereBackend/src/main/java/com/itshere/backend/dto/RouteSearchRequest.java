package com.itshere.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class RouteSearchRequest {
    @NotNull
    private Long userId;
    @Valid
    @NotNull
    private CoordinateDto start;
    @Valid
    @NotNull
    private CoordinateDto end;
    private Integer radiusMeters = 500;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CoordinateDto getStart() {
        return start;
    }

    public void setStart(CoordinateDto start) {
        this.start = start;
    }

    public CoordinateDto getEnd() {
        return end;
    }

    public void setEnd(CoordinateDto end) {
        this.end = end;
    }

    public Integer getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Integer radiusMeters) {
        this.radiusMeters = radiusMeters;
    }
}
