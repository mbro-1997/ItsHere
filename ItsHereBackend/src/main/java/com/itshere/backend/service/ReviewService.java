package com.itshere.backend.service;

import com.itshere.backend.dto.ReviewSaveRequest;
import com.itshere.backend.entity.PoiReviewEntity;
import com.itshere.backend.mapper.PoiReviewMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final PoiReviewMapper reviewMapper;

    public ReviewService(PoiReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    public List<PoiReviewEntity> listMine(Long userId) {
        return reviewMapper.findMine(userId);
    }

    public PoiReviewEntity save(ReviewSaveRequest request) {
        PoiReviewEntity review = new PoiReviewEntity();
        review.setUserId(request.getUserId());
        review.setPoiId(request.getPoiId());
        review.setPoiType(request.getPoiType());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setVisibility(normalizeVisibility(request.getVisibility()));
        reviewMapper.upsert(review);
        return reviewMapper.findMineForPoi(review.getUserId(), review.getPoiType(), review.getPoiId());
    }

    private String normalizeVisibility(String visibility) {
        if ("public".equals(visibility)) {
            return "public";
        }
        return "private";
    }
}
