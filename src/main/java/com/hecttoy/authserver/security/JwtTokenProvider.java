package com.hecttoy.authserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret:mySecretKeyForJWTTokenGenerationThatIsAtLeast32CharactersLongForHS256}")
    private String jwtSecret;

    @Value("${app.jwtAccessTokenExpiration:900000}") // 15 minutes in milliseconds
    private long jwtAccessTokenExpiration;

    @Value("${app.jwtRefreshTokenExpiration:604800000}") // 7 days in milliseconds
    private long jwtRefreshTokenExpiration;

    public String generateAccessToken(String username, Map<String, Object> claims) {
        return createToken(username, claims, jwtAccessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(username, claims, jwtRefreshTokenExpiration);
    }

    private String createToken(String username, Map<String, Object> claims, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error getting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public Long getExpirationTime(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.getExpiration().getTime();
        } catch (Exception e) {
            log.error("Error getting expiration time: {}", e.getMessage());
            return null;
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
