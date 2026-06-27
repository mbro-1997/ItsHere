package com.itshere.backend.service;

import com.itshere.backend.common.BusinessException;
import com.itshere.backend.dto.PlaceDetailDto;
import com.itshere.backend.dto.PlaceSummaryDto;
import com.itshere.backend.entity.PoiPlaceEntity;
import com.itshere.backend.entity.UserPlaceEntity;
import com.itshere.backend.mapper.PoiPlaceMapper;
import com.itshere.backend.mapper.PoiReviewMapper;
import com.itshere.backend.mapper.PoiTagMapper;
import com.itshere.backend.mapper.UserPlaceMapper;
import org.springframework.stereotype.Service;

@Service
public class PlaceService {

    private final PoiPlaceMapper placeMapper;
    private final UserPlaceMapper userPlaceMapper;
    private final PoiReviewMapper reviewMapper;
    private final PoiTagMapper tagMapper;
    private final PlaceAssembler assembler;

    public PlaceService(PoiPlaceMapper placeMapper,
                        UserPlaceMapper userPlaceMapper,
                        PoiReviewMapper reviewMapper,
                        PoiTagMapper tagMapper,
                        PlaceAssembler assembler) {
        this.placeMapper = placeMapper;
        this.userPlaceMapper = userPlaceMapper;
        this.reviewMapper = reviewMapper;
        this.tagMapper = tagMapper;
        this.assembler = assembler;
    }

    public PlaceDetailDto getDetail(Long userId, String poiType, Long poiId) {
        PlaceSummaryDto summary;
        if ("user".equals(poiType)) {
            UserPlaceEntity place = userPlaceMapper.findMineById(userId, poiId);
            if (place == null) {
                throw new BusinessException("个人点位不存在");
            }
            summary = assembler.toUserSummary(userId, place, null);
        } else {
            PoiPlaceEntity place = placeMapper.findById(poiId);
            if (place == null) {
                throw new BusinessException("系统点位不存在");
            }
            summary = assembler.toSystemSummary(userId, place, null);
        }

        PlaceDetailDto detail = new PlaceDetailDto();
        detail.setPlace(summary);
        detail.setReviews(reviewMapper.findVisibleForPoi(userId, summary.getPoiType(), summary.getId()));
        detail.setTags(summary.getTags());
        detail.setTagBadges(summary.getTagBadges());
        return detail;
    }
}
