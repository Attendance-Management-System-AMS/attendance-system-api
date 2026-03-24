package com.auth.service;

import com.auth.dto.*;
import com.auth.entity.Role;
import com.auth.entity.TokenBlacklist;
import com.auth.entity.User;
import com.auth.repository.RoleRepository;
import com.auth.repository.TokenBlacklistRepository;
import com.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ten dang nhap va mat khau la bat buoc");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ten dang nhap da ton tai");
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email da ton tai");
        }

        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName("ROLE_USER")
                        .description("Default user role")
                        .build()));

        User user = User.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail() == null ? null : request.getEmail().trim())
                .isEnabled(true)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ten dang nhap va mat khau la bat buoc");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tai khoan da bi khoa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai ten dang nhap hoac mat khau");
        }

        return buildAuthResponse(user);
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        try {
            var claims = jwtService.parseClaims(request.getToken());

            if (tokenBlacklistRepository.existsByTokenJti(claims.getId())) {
                return IntrospectResponse.invalid();
            }

            return IntrospectResponse.builder()
                    .valid(true)
                    .username(claims.getSubject())
                    .roles(claims.get("roles", String.class))
                    .expiresAt(claims.getExpiration().toInstant())
                    .build();
        } catch (JwtException | IllegalArgumentException ex) {
            return IntrospectResponse.invalid();
        }
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thieu refresh token");
            }

            if (!"REFRESH".equals(jwtService.getTokenType(refreshToken))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token khong phai refresh token");
            }

            String jti = jwtService.getJti(refreshToken);
            if (tokenBlacklistRepository.existsByTokenJti(jti)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token khong con hieu luc");
            }

            String username = jwtService.getUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nguoi dung khong ton tai"));

            if (!user.isEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tai khoan da bi khoa");
            }

            blacklistToken(refreshToken, user.getId());
            return buildAuthResponse(user);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token khong hop le");
        }
    }

    @Transactional
    public void logout(String token) {
        try {
            if (token == null || token.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thieu token dang xuat");
            }

            String tokenType = jwtService.getTokenType(token);
            if (!"ACCESS".equals(tokenType) && !"REFRESH".equals(tokenType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loai token khong hop le");
            }

            String username = jwtService.getUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nguoi dung khong ton tai"));

            blacklistToken(token, user.getId());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token khong hop le");
        }
    }

    private void blacklistToken(String token, Long userId) {
        String jti = jwtService.getJti(token);
        var expiration = jwtService.getExpiration(token);

        TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                .userId(userId)
                .tokenJti(jti)
                .expiresAt(OffsetDateTime.ofInstant(expiration, ZoneOffset.UTC))
                .build();

        try {
            tokenBlacklistRepository.save(blacklistedToken);
        } catch (DataIntegrityViolationException ex) {
            // Idempotent: token da ton tai trong blacklist
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);

        String accessToken = jwtService.generateAccessToken(user.getUsername(), claims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtService.getAccessTokenExpirationSeconds())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationSeconds())
                .username(user.getUsername())
                .roles(roles)
                .build();
    }
}
