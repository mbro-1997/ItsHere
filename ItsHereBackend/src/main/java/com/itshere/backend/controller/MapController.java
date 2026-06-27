package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.LocationSuggestionDto;
import com.itshere.backend.service.TencentMapClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class MapController {

    private final TencentMapClient tencentMapClient;

    public MapController(TencentMapClient tencentMapClient) {
        this.tencentMapClient = tencentMapClient;
    }

    @GetMapping("/reverse-geocode")
    public ApiResponse<String> reverseGeocode(@RequestParam double latitude, @RequestParam double longitude) {
        return ApiResponse.ok(tencentMapClient.reverseGeocode(latitude, longitude));
    }

    @GetMapping("/suggestion")
    public ApiResponse<List<LocationSuggestionDto>> suggestion(@RequestParam String keyword,
                                                               @RequestParam(defaultValue = "济南市") String region) {
        return ApiResponse.ok(tencentMapClient.suggestLocations(keyword, region));
    }
}
