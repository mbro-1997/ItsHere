package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.entity.PoiCategoryEntity;
import com.itshere.backend.mapper.PoiCategoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final PoiCategoryMapper categoryMapper;

    public CategoryController(PoiCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public ApiResponse<List<PoiCategoryEntity>> list() {
        return ApiResponse.ok(categoryMapper.findActive());
    }
}
