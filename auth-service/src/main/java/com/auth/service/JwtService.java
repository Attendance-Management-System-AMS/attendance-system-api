package com.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final String issuer;

    // Khởi tạo service JWT từ các cấu hình secret và thời hạn token.
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpirationMs,
            @Value("${jwt.issuer:auth-service}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.issuer = issuer;
    }

    // Tạo access token kèm claim phụ.
    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setId(UUID.randomUUID().toString())
                .claim("token_type", "ACCESS")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Tạo refresh token không cần claim phụ.
    public String generateRefreshToken(String subject) {
        return generateRefreshToken(subject, Collections.emptyMap());
    }

    // Tạo refresh token kèm claim phụ nếu cần.
    public String generateRefreshToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setId(UUID.randomUUID().toString())
                .claim("token_type", "REFRESH")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Giải mã token để lấy toàn bộ claims.
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Lấy subject từ token.
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    // Lấy username cũ nếu token cũ còn dùng claim này, hoặc fallback sang subject.
    public String getUsername(String token) {
        Claims claims = parseClaims(token);
        String usernameClaim = claims.get("username", String.class);
        if (usernameClaim != null && !usernameClaim.isBlank()) {
            return usernameClaim;
        }
        return claims.getSubject();
    }

    // Lấy loại token (ACCESS hoặc REFRESH).
    public String getTokenType(String token) {
        return parseClaims(token).get("token_type", String.class);
    }

    // Lấy mã định danh duy nhất của token.
    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    // Lấy thời điểm hết hạn của token.
    public Instant getExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    // Đổi thời gian hết hạn access token sang giây.
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000;
    }

    // Đổi thời gian hết hạn refresh token sang giây.
    public long getRefreshTokenExpirationSeconds() {
        return refreshTokenExpirationMs / 1000;
    }
}
