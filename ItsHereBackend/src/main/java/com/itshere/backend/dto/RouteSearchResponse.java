package com.itshere.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class RouteSearchResponse {
    private List<CoordinateDto> polyline = new ArrayList<>();
    private List<PlaceSummaryDto> places = new ArrayList<>();

    public List<CoordinateDto> getPolyline() {
        return polyline;
    }

    public void setPolyline(List<CoordinateDto> polyline) {
        this.polyline = polyline;
    }

    public List<PlaceSummaryDto> getPlaces() {
        return places;
    }

    public void setPlaces(List<PlaceSummaryDto> places) {
        this.places = places;
    }
}
