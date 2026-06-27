package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.BrandSuggestionRequest;
import com.itshere.backend.dto.BrandSuggestionResponse;
import com.itshere.backend.service.BrandSuggestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brand-suggestions")
public class BrandSuggestionController {

    private final BrandSuggestionService suggestionService;

    public BrandSuggestionController(BrandSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public ApiResponse<BrandSuggestionResponse> submit(@Valid @RequestBody BrandSuggestionRequest request) {
        return ApiResponse.ok(suggestionService.submit(request));
    }
}
