package com.attendance.service;

import com.attendance.dto.request.*;
import com.attendance.dto.response.*;
import com.attendance.entity.Role;
import com.attendance.entity.TokenBlacklist;
import com.attendance.entity.User;
import com.attendance.repository.TokenBlacklistRepository;
import com.attendance.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_HR = "ROLE_HR";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmployeeService employeeService;

    // Kiểm tra thông tin đăng nhập và trả về token.
    public AuthResponse login(LoginRequest request) {
        String loginIdentifier = firstNonBlank(request.getUsername(), request.getEmail());
        if (loginIdentifier == null || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập hoặc email và mật khẩu là bắt buộc");
        }

        String normalizedIdentifier = loginIdentifier.trim();
        User user = userRepository.findByUsername(normalizedIdentifier)
                .or(() -> userRepository.findByEmail(normalizedIdentifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu");
        }

        return buildAuthResponse(user);
    }

    // Làm mới access token bằng refresh token còn hiệu lực.
    // Hỗ trợ grace period 30 giây cho token đã bị rotation để tránh race condition.
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu refresh token");
            }

            Claims claims = jwtService.parseClaims(refreshToken);

            if (!"REFRESH".equals(claims.get("token_type", String.class))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token không phải refresh token");
            }

            String jti = claims.getId();

            // Kiểm tra blacklist với grace period
            var blacklistEntry = tokenBlacklistRepository.findByTokenJti(jti);
            if (blacklistEntry.isPresent()) {
                TokenBlacklist entry = blacklistEntry.get();
                OffsetDateTime blacklistedAt = entry.getBlacklistedAt();

                // Grace period 30 giây: Nếu token vừa bị blacklist bởi 1 request refresh khác
                // (ví dụ race condition từ nhiều tab), trả về cặp token của user hiện tại
                // thay vì reject.
                boolean withinGracePeriod = blacklistedAt != null
                        && blacklistedAt.plusSeconds(30).isAfter(OffsetDateTime.now(ZoneOffset.UTC));

                if (withinGracePeriod) {
                    log.warn("Refresh token replay trong grace period (jti={}). Cấp token mới.", jti);
                    User user = resolveUserFromClaims(claims);
                    if (!user.isEnabled()) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
                    }
                    return buildAuthResponse(user);
                }

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token không còn hiệu lực");
            }

            User user = resolveUserFromClaims(claims);

            if (!user.isEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
            }

            // Tạo cặp token mới TRƯỚC khi blacklist token cũ
            AuthResponse response = buildAuthResponse(user);

            // Blacklist token cũ, lưu JTI của token mới thay thế
            String newRefreshJti = jwtService.getJti(response.getRefreshToken());
            blacklistTokenWithReplacement(refreshToken, user.getId(), newRefreshJti);

            return response;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token không hợp lệ");
        }
    }

    // Đưa access hoặc refresh token vào blacklist.
    @Transactional
    public void logout(String authHeader) {
        String token = extractToken(authHeader);
        try {
            if (token == null || token.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu token đăng xuất");
            }

            Claims claims = jwtService.parseClaims(token);
            String tokenType = claims.get("token_type", String.class);
            if (!"ACCESS".equals(tokenType) && !"REFRESH".equals(tokenType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại token không hợp lệ");
            }

            User user = resolveUserFromClaims(claims);
            blacklistToken(token, user.getId());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token không hợp lệ");
        }
    }

    // Lấy thông tin tài khoản đang đăng nhập từ SecurityContext.
    public UserProfileResponse getCurrentUser() {
        User user = getCurrentAuthenticatedUser();

        return buildUserProfile(user);
    }

    private UserProfileResponse buildUserProfile(User user) {
        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(String.join(",", user.getRoles().stream().map(Role::getRoleName).toList()));

        // Lấy thêm thông tin từ hr-service thông qua userId (goi truc tiep Service)
        try {
            EmployeeInternalResponse employee = employeeService.getInternalEmployee(user.getId());
            if (employee != null) {
                builder.fullName(employee.getFullName())
                        .departmentName(employee.getDepartmentName())
                        .positionName(employee.getPositionName());
            }
        } catch (Exception e) {
            log.error("Không tìm thấy thông tin nhân viên cho userId: " + user.getId(), e);
        }

        return builder.build();
    }

    // Đổi mật khẩu cho tài khoản hiện tại.
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (request == null
                || request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại và mật khẩu mới là bắt buộc");
        }

        if (request.getNewPassword().trim().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        User user = getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        userRepository.save(user);
    }

    // Lấy User từ SecurityContext.
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập hoặc token không hợp lệ");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        
        if (principal instanceof String) {
            String subject = (String) principal;
            return resolveUserFromSubject(subject);
        }
        
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Principal không hợp lệ");
    }

    private User resolveUserFromSubject(String subject) {
        boolean numericSubject = subject.chars().allMatch(Character::isDigit);
        if (numericSubject) {
            Long userId = Long.parseLong(subject);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
        }
        return userRepository.findByUsername(subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
    }

    // Trích xuất token từ chuỗi header Bearer.
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }

    // Ghi một token vào blacklist để chặn sử dụng lại (dùng cho logout).
    private void blacklistToken(String token, Long userId) {
        blacklistTokenWithReplacement(token, userId, null);
    }

    // Ghi một token vào blacklist kèm JTI token thay thế (dùng cho refresh rotation).
    private void blacklistTokenWithReplacement(String token, Long userId, String replacedByJti) {
        String jti = jwtService.getJti(token);
        var expiration = jwtService.getExpiration(token);

        TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                .userId(userId)
                .tokenJti(jti)
                .expiresAt(OffsetDateTime.ofInstant(expiration, ZoneOffset.UTC))
                .replacedByJti(replacedByJti)
                .blacklistedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        try {
            tokenBlacklistRepository.save(blacklistedToken);
        } catch (DataIntegrityViolationException ex) {
            // Idempotent: token đã tồn tại trong blacklist
        }
    }

    // Tạo cặp access token và refresh token cho một user.
    private AuthResponse buildAuthResponse(User user) {
        List<String> roleNames = user.getRoles().stream().map(Role::getRoleName).toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);
        claims.put("username", user.getUsername());

        String accessToken = jwtService.generateAccessToken(String.valueOf(user.getId()), claims);
        String refreshToken = jwtService.generateRefreshToken(
            String.valueOf(user.getId()),
            Map.of("username", user.getUsername())
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresIn(jwtService.getAccessTokenExpirationSeconds())
                .refreshTokenExpiresIn(jwtService.getRefreshTokenExpirationSeconds())
                .user(buildUserProfile(user))
                .build();
    }
    // Tìm user theo subject token kiểu mới hoặc fallback theo username token cũ.
    private User resolveUserFromClaims(Claims claims) {
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token thiếu subject");
        }

        boolean numericSubject = subject.chars().allMatch(Character::isDigit);
        if (numericSubject) {
            Long userId = Long.parseLong(subject);
            return userRepository.findById(userId)
                    .orElseGet(() -> {
                        String usernameClaim = claims.get("username", String.class);
                        if (usernameClaim != null && !usernameClaim.isBlank()) {
                            return userRepository.findByUsername(usernameClaim)
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
                        }
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại");
                    });
        }

        return userRepository.findByUsername(subject)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));
    }

    // Lấy chuỗi đầu tiên không rỗng trong hai giá trị truyền vào.
    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

}




