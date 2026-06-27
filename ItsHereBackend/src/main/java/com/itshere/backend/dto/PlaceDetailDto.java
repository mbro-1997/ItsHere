package com.itshere.backend.dto;

import com.itshere.backend.entity.PoiReviewEntity;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailDto {
    private PlaceSummaryDto place;
    private List<PoiReviewEntity> reviews = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private List<TagBadgeDto> tagBadges = new ArrayList<>();

    public PlaceSummaryDto getPlace() {
        return place;
    }

    public void setPlace(PlaceSummaryDto place) {
        this.place = place;
    }

    public List<PoiReviewEntity> getReviews() {
        return reviews;
    }

    public void setReviews(List<PoiReviewEntity> reviews) {
        this.reviews = reviews;
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
