package com.hecttoy.authserver.controller;

import com.hecttoy.authserver.dto.*;
import com.hecttoy.authserver.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<UserInfoResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Register endpoint called for user: {}", registerRequest.getUsername());

        UserInfoResponse userInfo = authService.register(registerRequest);

        StandardResponse<UserInfoResponse> response = StandardResponse.success(
            HttpStatus.CREATED.value(),
            "User registered successfully",
            userInfo
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login endpoint called for email: {}", loginRequest.getEmail());

        TokenResponse tokenResponse = authService.login(loginRequest);

        StandardResponse<TokenResponse> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Login successful",
            tokenResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("Refresh token endpoint called");

        TokenResponse tokenResponse = authService.refresh(refreshTokenRequest);

        StandardResponse<TokenResponse> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Token refreshed successfully",
            tokenResponse
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest logoutRequest) {
        log.info("Logout endpoint called");

        authService.logout(logoutRequest);

        StandardResponse<Void> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "Logout successful",
            null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<StandardResponse<UserInfoResponse>> getUserInfo() {
        log.info("User info endpoint called");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserInfoResponse userInfo = authService.getUserInfo(username);

        StandardResponse<UserInfoResponse> response = StandardResponse.success(
            HttpStatus.OK.value(),
            "User info retrieved successfully",
            userInfo
        );

        return ResponseEntity.ok(response);
    }
}
