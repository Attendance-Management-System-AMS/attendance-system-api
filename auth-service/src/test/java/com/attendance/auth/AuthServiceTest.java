package com.attendance.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.client.HrClient;
import com.attendance.dto.request.LoginRequest;
import com.attendance.dto.request.RefreshTokenRequest;
import com.attendance.dto.response.AuthResponse;
import com.attendance.entity.Role;
import com.attendance.entity.User;
import com.attendance.repository.RoleRepository;
import com.attendance.repository.UserRepository;
import com.attendance.service.AuthService;
import com.attendance.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private HrClient hrClient;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginStoresRefreshTokenOnUser() {
        User user = user(1L, "admin");
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Secret@123");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret@123", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken(eq("1"), anyMap())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq("1"), anyMap())).thenReturn("refresh-token");
        when(jwtService.getExpiration("refresh-token")).thenReturn(Instant.parse("2026-04-26T00:00:00Z"));
        when(passwordEncoder.encode("refresh-token")).thenReturn("hashed-refresh-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(user.getRefreshTokenHash()).isEqualTo("hashed-refresh-token");
        assertThat(user.getRefreshTokenExpiresAt()).isEqualTo(OffsetDateTime.parse("2026-04-26T00:00:00Z"));
        verify(userRepository).save(user);
    }

    @Test
    void refreshReplacesStoredRefreshToken() {
        User user = user(1L, "admin");
        user.setRefreshTokenHash("stored-refresh-hash");
        user.setRefreshTokenExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh-token");

        Claims claims = Jwts.claims();
        claims.setSubject("1");
        claims.put("token_type", "REFRESH");
        claims.put("username", "admin");

        when(jwtService.parseClaims("old-refresh-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-refresh-token", "stored-refresh-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(eq("1"), anyMap())).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(eq("1"), anyMap())).thenReturn("new-refresh-token");
        when(jwtService.getExpiration("new-refresh-token")).thenReturn(Instant.parse("2026-04-27T00:00:00Z"));
        when(passwordEncoder.encode("new-refresh-token")).thenReturn("new-refresh-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(user.getRefreshTokenHash()).isEqualTo("new-refresh-hash");
        assertThat(user.getRefreshTokenExpiresAt()).isEqualTo(OffsetDateTime.parse("2026-04-27T00:00:00Z"));
        verify(userRepository).save(user);
    }

    @Test
    void logoutClearsStoredRefreshTokenForAuthenticatedUser() {
        User user = user(1L, "admin");
        user.setRefreshTokenHash("stored-refresh-hash");
        user.setRefreshTokenExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));

        Claims claims = Jwts.claims();
        claims.setSubject("1");
        claims.put("token_type", "ACCESS");
        claims.put("username", "admin");

        when(jwtService.parseClaims("access-token")).thenReturn(claims);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.logout("Bearer access-token", null);

        assertThat(user.getRefreshTokenHash()).isNull();
        assertThat(user.getRefreshTokenExpiresAt()).isNull();
        verify(userRepository).save(user);
    }

    private User user(Long id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .password("encoded-password")
                .email(username + "@company.com")
                .isEnabled(true)
                .roles(Set.of(Role.builder().id(1L).roleName("ROLE_ADMIN").build()))
                .build();
    }
}
