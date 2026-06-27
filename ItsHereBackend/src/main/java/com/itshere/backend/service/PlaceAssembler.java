package com.itshere.backend.service;

import com.itshere.backend.dto.PlaceSummaryDto;
import com.itshere.backend.dto.TagBadgeDto;
import com.itshere.backend.entity.PoiPlaceEntity;
import com.itshere.backend.entity.UserPlaceEntity;
import com.itshere.backend.mapper.PoiReviewMapper;
import com.itshere.backend.mapper.PoiTagMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaceAssembler {

    private final PoiReviewMapper reviewMapper;
    private final PoiTagMapper tagMapper;

    public PlaceAssembler(PoiReviewMapper reviewMapper, PoiTagMapper tagMapper) {
        this.reviewMapper = reviewMapper;
        this.tagMapper = tagMapper;
    }

    public PlaceSummaryDto toSystemSummary(Long userId, PoiPlaceEntity place, Integer distanceMeters) {
        PlaceSummaryDto dto = new PlaceSummaryDto();
        dto.setId(place.getId());
        dto.setPoiType("system");
        dto.setName(place.getName());
        dto.setBrandId(place.getBrandId());
        dto.setBrandName(place.getBrandName());
        dto.setCategoryId(place.getCategoryId());
        dto.setBusinessCategory(place.getBusinessCategory());
        dto.setAddress(place.getAddress());
        dto.setPhone(place.getPhone());
        dto.setLongitude(place.getLongitude() == null ? null : place.getLongitude().doubleValue());
        dto.setLatitude(place.getLatitude() == null ? null : place.getLatitude().doubleValue());
        dto.setDistanceMeters(distanceMeters);
        fillPersonalSignals(userId, dto);
        return dto;
    }

    public PlaceSummaryDto toUserSummary(Long userId, UserPlaceEntity place, Integer distanceMeters) {
        PlaceSummaryDto dto = new PlaceSummaryDto();
        dto.setId(place.getId());
        dto.setPoiType("user");
        dto.setName(place.getName());
        dto.setBrandName(place.getBrandName());
        dto.setCategoryId(place.getCategoryId());
        dto.setBusinessCategory("个人点位");
        dto.setAddress(place.getAddress());
        dto.setLongitude(place.getLongitude() == null ? null : place.getLongitude().doubleValue());
        dto.setLatitude(place.getLatitude() == null ? null : place.getLatitude().doubleValue());
        dto.setDistanceMeters(distanceMeters);
        fillPersonalSignals(userId, dto);
        return dto;
    }

    public void fillPersonalSignals(Long userId, PlaceSummaryDto dto) {
        if (userId == null || dto.getId() == null) {
            return;
        }
        dto.setHasMyReview(reviewMapper.countMineForPoi(userId, dto.getPoiType(), dto.getId()) > 0);
        List<TagBadgeDto> tagBadges = tagMapper.findVisibleTagBadgesForPoi(userId, dto.getPoiType(), dto.getId());
        dto.setTagBadges(tagBadges);
        dto.setTags(tagBadges.stream().map(TagBadgeDto::getName).toList());
    }
}
