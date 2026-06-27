package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.PlaceDetailDto;
import com.itshere.backend.service.PlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/{poiType}/{poiId}")
    public ApiResponse<PlaceDetailDto> detail(@RequestParam Long userId,
                                              @PathVariable String poiType,
                                              @PathVariable Long poiId) {
        return ApiResponse.ok(placeService.getDetail(userId, poiType, poiId));
    }
}
