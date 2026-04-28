package com.attendance.service;

import com.attendance.dto.response.KioskSessionResponse;
import com.attendance.exception.AppException;
import com.attendance.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KioskAccessService {

    public static final String HEADER_DEVICE_ID = "X-Kiosk-Device-Id";
    public static final String HEADER_SESSION = "X-Kiosk-Session";
    public static final String HEADER_NONCE = "X-Kiosk-Nonce";
    public static final String HEADER_TIMESTAMP = "X-Kiosk-Timestamp";

    private static final int MAX_DEVICE_ID_LENGTH = 128;
    private static final int MAX_NONCE_LENGTH = 128;
    private static final int MAX_TIMESTAMP_LENGTH = 32;
    private static final int MAX_SESSION_LENGTH = 4096;

    private final JwtService jwtService;
    private final Clock clock;
    private final long tokenExpirationSeconds;
    private final long allowedClockSkewSeconds;
    private final long nonceTtlSeconds;
    private final ConcurrentMap<String, Instant> nonceLedger = new ConcurrentHashMap<>();

    public KioskAccessService(
            JwtService jwtService,
            Clock clock,
            @Value("${app.attendance.kiosk.token-expiration-seconds:300}") long tokenExpirationSeconds,
            @Value("${app.attendance.kiosk.allowed-clock-skew-seconds:30}") long allowedClockSkewSeconds,
            @Value("${app.attendance.kiosk.nonce-ttl-seconds:120}") long nonceTtlSeconds) {
        this.jwtService = jwtService;
        this.clock = clock;
        this.tokenExpirationSeconds = tokenExpirationSeconds;
        this.allowedClockSkewSeconds = allowedClockSkewSeconds;
        this.nonceTtlSeconds = nonceTtlSeconds;
    }

    public KioskSessionResponse issueSession(String operatorSubject, String deviceId) {
        if (operatorSubject == null || operatorSubject.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Phiên vận hành kiosk không hợp lệ");
        }

        String normalizedDeviceId = requireHeaderValue(deviceId, "mã thiết bị kiosk", MAX_DEVICE_ID_LENGTH);
        Instant now = clock.instant();
        String token = jwtService.generateKioskToken(
                operatorSubject.trim(),
                Map.of("device_id", normalizedDeviceId),
                tokenExpirationSeconds);

        return new KioskSessionResponse(
                token,
                "KIOSK",
                normalizedDeviceId,
                tokenExpirationSeconds,
                now.plusSeconds(tokenExpirationSeconds));
    }

    public String validateScanRequest(String kioskToken, String deviceId, String nonce, String timestampHeader) {
        String normalizedDeviceId = requireHeaderValue(deviceId, "mã thiết bị kiosk", MAX_DEVICE_ID_LENGTH);
        String normalizedNonce = requireHeaderValue(nonce, "nonce kiosk", MAX_NONCE_LENGTH);
        String normalizedToken = requireHeaderValue(kioskToken, "phiên kiosk", MAX_SESSION_LENGTH);
        Instant requestTime = parseRequestTime(timestampHeader);
        Instant now = clock.instant();

        if (requestTime.isBefore(now.minusSeconds(allowedClockSkewSeconds))
                || requestTime.isAfter(now.plusSeconds(allowedClockSkewSeconds))) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Yêu cầu kiosk đã hết hạn hoặc sai thời gian");
        }

        pruneExpiredNonces(now);

        try {
            Claims claims = jwtService.parseClaims(normalizedToken);
            if (!"KIOSK".equals(claims.get("token_type", String.class))) {
                throw new AppException(ErrorCode.FORBIDDEN, "Phiên kiosk không hợp lệ");
            }

            String claimedDeviceId = claims.get("device_id", String.class);
            if (claimedDeviceId == null || !claimedDeviceId.equals(normalizedDeviceId)) {
                throw new AppException(ErrorCode.FORBIDDEN, "Thiết bị kiosk không khớp với phiên hiện tại");
            }

            String nonceKey = claims.getId() + ":" + normalizedNonce;
            Instant nonceExpiresAt = now.plusSeconds(Math.max(nonceTtlSeconds, allowedClockSkewSeconds));
            if (nonceLedger.putIfAbsent(nonceKey, nonceExpiresAt) != null) {
                throw new AppException(ErrorCode.FORBIDDEN, "Yêu cầu kiosk đã được sử dụng trước đó");
            }

            return normalizedDeviceId;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AppException(ErrorCode.FORBIDDEN, "Phiên kiosk không hợp lệ hoặc đã hết hạn");
        }
    }

    private Instant parseRequestTime(String timestampHeader) {
        String normalizedTimestamp = requireHeaderValue(timestampHeader, "thời gian kiosk", MAX_TIMESTAMP_LENGTH);
        try {
            return Instant.ofEpochMilli(Long.parseLong(normalizedTimestamp));
        } catch (NumberFormatException ex) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Thời gian kiosk không đúng định dạng");
        }
    }

    private String requireHeaderValue(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new AppException(ErrorCode.FORBIDDEN, "Thiếu " + fieldName + " hợp lệ");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Giá trị " + fieldName + " vượt quá độ dài cho phép");
        }
        return normalized;
    }

    private void pruneExpiredNonces(Instant now) {
        nonceLedger.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
