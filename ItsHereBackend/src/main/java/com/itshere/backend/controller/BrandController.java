package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.BrandConfigRequest;
import com.itshere.backend.entity.PoiBrandEntity;
import com.itshere.backend.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ApiResponse<List<PoiBrandEntity>> list(@RequestParam Long userId) {
        return ApiResponse.ok(brandService.listForUser(userId));
    }

    @PostMapping("/config")
    public ApiResponse<Void> saveConfig(@Valid @RequestBody BrandConfigRequest request) {
        brandService.saveConfig(request.getUserId(), request.getBrandId(), request.getEnabled());
        return ApiResponse.ok(null);
    }
}
