package com.hecttoy.authserver.service;

import com.hecttoy.authserver.dto.*;
import com.hecttoy.authserver.exception.AuthException;
import com.hecttoy.authserver.exception.ResourceNotFoundException;
import com.hecttoy.authserver.exception.TokenException;
import com.hecttoy.authserver.model.RefreshToken;
import com.hecttoy.authserver.model.Role;
import com.hecttoy.authserver.model.User;
import com.hecttoy.authserver.repository.RefreshTokenRepository;
import com.hecttoy.authserver.repository.RoleRepository;
import com.hecttoy.authserver.repository.UserRepository;
import com.hecttoy.authserver.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserInfoResponse register(RegisterRequest registerRequest) {
        log.info("Registering new user with username: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AuthException(409, "Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AuthException(409, "Email already exists");
        }

        Role defaultRole = roleRepository.findByName("USER")
            .orElseGet(() -> roleRepository.save(Role.builder()
                .name("USER")
                .description("Default user role")
                .build()));

        User user = User.builder()
            .username(registerRequest.getUsername())
            .email(registerRequest.getEmail())
            .password(passwordEncoder.encode(registerRequest.getPassword()))
            .firstName(registerRequest.getFirstName())
            .lastName(registerRequest.getLastName())
            .active(true)
            .roles(new HashSet<>(Collections.singletonList(defaultRole)))
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        return mapUserToResponse(savedUser);
    }

    public TokenResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new AuthException(401, "Invalid email or password"));

        if (!user.getActive()) {
            throw new AuthException(403, "User account is inactive");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthException(401, "Invalid email or password");
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        log.info("User {} logged in successfully", user.getEmail());

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900L) // 15 minutes
            .scope("read write")
            .build();
    }

    public TokenResponse refresh(RefreshTokenRequest refreshTokenRequest) {
        log.info("Refreshing access token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshToken())
            .orElseThrow(() -> new TokenException("Invalid refresh token"));

        if (refreshToken.isExpired() || refreshToken.getRevoked()) {
            throw new TokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        if (!user.getActive()) {
            throw new AuthException(403, "User account is inactive");
        }

        String newAccessToken = generateAccessToken(user);
        log.info("Access token refreshed for user: {}", user.getEmail());

        return TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshTokenRequest.getRefreshToken())
            .tokenType("Bearer")
            .expiresIn(900L) // 15 minutes
            .scope("read write")
            .build();
    }

    public void logout(LogoutRequest logoutRequest) {
        log.info("Logout attempt");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(logoutRequest.getRefreshToken())
            .orElseThrow(() -> new TokenException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        log.info("User logged out successfully");
    }

    public UserInfoResponse getUserInfo(String username) {
        log.info("Fetching user info for username: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapUserToResponse(user);
    }

    private String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));

        return jwtTokenProvider.generateAccessToken(user.getUsername(), claims);
    }

    private String generateRefreshToken(User user) {
        String token = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // Revoke old refresh tokens
        refreshTokenRepository.deleteByUserId(user.getId());

        // Save new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusDays(7))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private UserInfoResponse mapUserToResponse(User user) {
        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .active(user.getActive())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
