package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.ReviewSaveRequest;
import com.itshere.backend.entity.PoiReviewEntity;
import com.itshere.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/mine")
    public ApiResponse<List<PoiReviewEntity>> mine(@RequestParam Long userId) {
        return ApiResponse.ok(reviewService.listMine(userId));
    }

    @PostMapping
    public ApiResponse<PoiReviewEntity> save(@Valid @RequestBody ReviewSaveRequest request) {
        return ApiResponse.ok(reviewService.save(request));
    }
}
