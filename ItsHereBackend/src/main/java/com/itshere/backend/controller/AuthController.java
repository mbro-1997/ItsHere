package com.itshere.backend.controller;

import com.itshere.backend.common.ApiResponse;
import com.itshere.backend.dto.AuthLoginRequest;
import com.itshere.backend.dto.AuthLoginResponse;
import com.itshere.backend.service.WechatAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final WechatAuthService authService;

    public AuthController(WechatAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ApiResponse.ok(authService.loginByCode(request.getCode()));
    }

    @PostMapping("/dev-login")
    public ApiResponse<AuthLoginResponse> devLogin() {
        return ApiResponse.ok(authService.devLogin());
    }
}
