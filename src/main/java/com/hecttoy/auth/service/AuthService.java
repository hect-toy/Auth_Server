package com.hecttoy.auth.service;

import com.hecttoy.auth.dto.auth.*;
import com.hecttoy.auth.entity.RefreshToken;
import com.hecttoy.auth.entity.Role;
import com.hecttoy.auth.entity.User;
import com.hecttoy.auth.exception.AuthException;
import com.hecttoy.auth.repository.RefreshTokenRepository;
import com.hecttoy.auth.repository.RoleRepository;
import com.hecttoy.auth.repository.UserRepository;
import com.hecttoy.auth.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      AuthenticationManager authenticationManager,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException ex) {
            throw new AuthException("Credenciales inválidas");
        }

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        if (!user.isEnabled()) {
            throw new AuthException("Usuario deshabilitado");
        }

        return generateAuthResponse(user);
    }

    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new AuthException("Las contraseñas no coinciden");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email ya está registrado");
        }

        // Crear rol USER por defecto si no existe
        Role userRole = roleRepository.findByName("USER")
            .orElseGet(() -> {
                Role role = Role.builder()
                    .name("USER")
                    .description("Usuario regular")
                    .build();
                return roleRepository.save(role);
            });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .enabled(true)
            .roles(roles)
            .build();

        userRepository.save(user);
        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new AuthException("Refresh token inválido o no encontrado"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthException("Refresh token ha expirado o fue revocado");
        }

        User user = refreshToken.getUser();
        return generateAuthResponse(user);
    }

    public void logout(String token) {
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthException("Usuario no encontrado"));

        refreshTokenRepository.deleteByUser(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Guardar refresh token en BD
        RefreshToken token = RefreshToken.builder()
            .token(refreshToken)
            .user(user)
            .expiryDate(LocalDateTime.now().plusDays(7))
            .build();
        refreshTokenRepository.save(token);

        UserInfoDto userInfo = UserInfoDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet()))
            .enabled(user.isEnabled())
            .build();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
            .user(userInfo)
            .build();
    }
}
