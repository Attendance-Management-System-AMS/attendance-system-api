package com.attendance.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.attendance.dto.response.KioskSessionResponse;
import com.attendance.exception.AppException;
import com.attendance.service.JwtService;
import com.attendance.service.KioskAccessService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KioskAccessServiceTest {

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final Instant NOW = Instant.parse("2026-04-25T03:00:00Z");

    private KioskAccessService kioskAccessService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, BANGKOK);
        JwtService jwtService = new JwtService(
                "12345678901234567890123456789012",
                900_000,
                604_800_000,
                "attendance-auth-service");
        kioskAccessService = new KioskAccessService(jwtService, clock, 300, 30, 120);
    }

    @Test
    void issueSessionAndValidateRequestSucceeds() {
        KioskSessionResponse session = kioskAccessService.issueSession("12", "kiosk-a");

        String validatedDeviceId = kioskAccessService.validateScanRequest(
                session.token(),
                "kiosk-a",
                "nonce-1",
                String.valueOf(NOW.toEpochMilli()));

        assertEquals("kiosk-a", validatedDeviceId);
        assertEquals("KIOSK", session.tokenType());
    }

    @Test
    void validateScanRequestRejectsReplayNonce() {
        KioskSessionResponse session = kioskAccessService.issueSession("12", "kiosk-a");

        kioskAccessService.validateScanRequest(
                session.token(),
                "kiosk-a",
                "nonce-1",
                String.valueOf(NOW.toEpochMilli()));

        AppException exception = assertThrows(
                AppException.class,
                () -> kioskAccessService.validateScanRequest(
                        session.token(),
                        "kiosk-a",
                        "nonce-1",
                        String.valueOf(NOW.toEpochMilli())));

        assertEquals("Yêu cầu kiosk đã được sử dụng trước đó", exception.getMessage());
    }

    @Test
    void validateScanRequestRejectsExpiredTimestamp() {
        KioskSessionResponse session = kioskAccessService.issueSession("12", "kiosk-a");

        AppException exception = assertThrows(
                AppException.class,
                () -> kioskAccessService.validateScanRequest(
                        session.token(),
                        "kiosk-a",
                        "nonce-2",
                        String.valueOf(NOW.minusSeconds(31).toEpochMilli())));

        assertEquals("Yêu cầu kiosk đã hết hạn hoặc sai thời gian", exception.getMessage());
    }
}
