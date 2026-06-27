package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.RouteSearchRequest;
import com.itshere.backend.dto.RouteSearchResponse;
import com.itshere.backend.service.RouteSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteSearchService routeSearchService;

    public RouteController(RouteSearchService routeSearchService) {
        this.routeSearchService = routeSearchService;
    }

    @PostMapping("/search")
    public ApiResponse<RouteSearchResponse> search(@Valid @RequestBody RouteSearchRequest request) {
        return ApiResponse.ok(routeSearchService.search(request));
    }
}
