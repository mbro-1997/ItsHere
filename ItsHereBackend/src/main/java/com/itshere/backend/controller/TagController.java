package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.TagSaveRequest;
import com.itshere.backend.entity.PoiTagEntity;
import com.itshere.backend.service.TagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/mine")
    public ApiResponse<List<PoiTagEntity>> mine(@RequestParam Long userId) {
        return ApiResponse.ok(tagService.listMine(userId));
    }

    @GetMapping("/poi")
    public ApiResponse<List<PoiTagEntity>> mineForPoi(@RequestParam Long userId,
                                                      @RequestParam String poiType,
                                                      @RequestParam Long poiId) {
        return ApiResponse.ok(tagService.listMineForPoi(userId, poiType, poiId));
    }

    @PostMapping
    public ApiResponse<List<PoiTagEntity>> save(@Valid @RequestBody TagSaveRequest request) {
        return ApiResponse.ok(tagService.saveForPoi(request));
    }

    @DeleteMapping
    public ApiResponse<List<PoiTagEntity>> delete(@RequestParam Long userId,
                                                  @RequestParam String poiType,
                                                  @RequestParam Long poiId,
                                                  @RequestParam String tagName) {
        return ApiResponse.ok(tagService.deleteForPoi(userId, poiType, poiId, tagName));
    }
}
