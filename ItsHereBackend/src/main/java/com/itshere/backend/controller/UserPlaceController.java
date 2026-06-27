package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.UserPlaceSaveRequest;
import com.itshere.backend.entity.UserPlaceEntity;
import com.itshere.backend.service.UserPlaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-places")
public class UserPlaceController {

    private final UserPlaceService userPlaceService;

    public UserPlaceController(UserPlaceService userPlaceService) {
        this.userPlaceService = userPlaceService;
    }

    @GetMapping
    public ApiResponse<List<UserPlaceEntity>> mine(@RequestParam Long userId) {
        return ApiResponse.ok(userPlaceService.listMine(userId));
    }

    @PostMapping
    public ApiResponse<UserPlaceEntity> save(@Valid @RequestBody UserPlaceSaveRequest request) {
        return ApiResponse.ok(userPlaceService.save(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@org.springframework.web.bind.annotation.PathVariable Long id,
                                    @RequestParam Long userId) {
        userPlaceService.deleteMine(userId, id);
        return ApiResponse.ok(null);
    }
}
