package com.hecttoy.auth.controller;

import com.hecttoy.auth.dto.StandardResponse;
import com.hecttoy.auth.dto.auth.*;
import com.hecttoy.auth.security.JwtTokenProvider;
import com.hecttoy.auth.service.AuthService;
import com.hecttoy.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        StandardResponse<AuthResponse> response = StandardResponse.ok(
            authResponse,
            "Login exitoso"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        StandardResponse<AuthResponse> response = StandardResponse.created(
            authResponse,
            "Usuario registrado exitosamente"
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        StandardResponse<AuthResponse> response = StandardResponse.ok(
            authResponse,
            "Token renovado exitosamente"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        authService.logout(jwtToken);
        StandardResponse<Void> response = StandardResponse.ok(
            null,
            "Logout exitoso"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/userinfo")
    public ResponseEntity<StandardResponse<UserInfoDto>> getUserInfo(Authentication authentication) {
        String email = authentication.getName();
        UserInfoDto userInfo = userService.getUserByEmail(email);
        StandardResponse<UserInfoDto> response = StandardResponse.ok(
            userInfo,
            "Información de usuario obtenida exitosamente"
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
