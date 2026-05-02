package com.attendance.service;

import com.attendance.client.HrClient;
import com.attendance.dto.request.*;
import com.attendance.dto.response.*;
import com.attendance.entity.Role;
import com.attendance.entity.User;
import com.attendance.repository.RoleRepository;
import com.attendance.repository.UserRepository;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HrClient hrClient;

    // Kiểm tra thông tin đăng nhập và trả về token.
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String loginIdentifier = firstNonBlank(request.getUsername(), request.getEmail());
        if (loginIdentifier == null || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập hoặc email và mật khẩu là bắt buộc");
        }

        String normalizedIdentifier = loginIdentifier.trim();
        User user = findUserByLoginIdentifier(normalizedIdentifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu"));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu");
        }

        return issueTokensForUser(user);
    }

    @Transactional
    public InternalUserResponse createInternalUser(InternalCreateUserRequest request) {
        String username = request.username() == null ? null : request.username().trim();
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập là bắt buộc");
        }
        if (Boolean.TRUE.equals(userRepository.existsByUsernameIgnoreCase(username))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        }

        String email = request.email() == null ? null : request.email().trim();
        if (email != null && !email.isBlank() && Boolean.TRUE.equals(userRepository.existsByEmailIgnoreCase(email))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
        }

        List<Role> roles = roleRepository.findByRoleNameIn(request.roles());
        if (roles.size() != request.roles().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vai trò không hợp lệ");
        }

        User saved = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password().trim()))
                .email(email == null || email.isBlank() ? null : email)
                .isEnabled(request.enabled())
                .roles(Set.copyOf(roles))
                .build());

        return toInternalUserResponse(saved);
    }

    @Transactional
    public InternalUserResponse updateInternalUser(Long id, InternalUpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        String username = request.username() == null ? null : request.username().trim();
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập là bắt buộc");
        }

        findOtherUserWithSameUsername(user.getId(), username)
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
                });

        String email = request.email() == null ? null : request.email().trim();
        if (email != null && !email.isBlank()) {
            findOtherUserWithSameEmail(user.getId(), email)
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");
                });
        }

        user.setUsername(username);
        user.setEmail(email == null || email.isBlank() ? null : email);
        user.setEnabled(request.enabled());
        if (!request.enabled()) {
            user.setRefreshTokenHash(null);
            user.setRefreshTokenExpiresAt(null);
        }

        return toInternalUserResponse(userRepository.save(user));
    }

    // Làm mới access token bằng refresh token còn hiệu lực được lưu trên tài khoản.
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

            User user = resolveUserFromClaims(claims);

            if (!user.isEnabled()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
            }

            validateStoredRefreshToken(user, refreshToken.trim());
            return issueTokensForUser(user);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token không hợp lệ");
        }
    }

    // Đăng xuất bằng cách xoá refresh token đang được lưu trên tài khoản.
    @Transactional
    public void logout(String authHeader, String refreshToken) {
        if ((authHeader == null || authHeader.isBlank()) && (refreshToken == null || refreshToken.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu token đăng xuất");
        }

        User accessUser = resolveUserFromAccessToken(extractToken(authHeader));
        User refreshUser = resolveUserFromStoredRefreshToken(refreshToken);

        if (accessUser == null && refreshUser == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy phiên đăng nhập hợp lệ để đăng xuất");
        }

        if (accessUser != null && refreshUser != null && !accessUser.getId().equals(refreshUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token đăng xuất không cùng người dùng");
        }

        clearStoredRefreshToken(accessUser != null ? accessUser : refreshUser);
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

        enrichUserProfileFromHr(builder, user.getId());

        return builder.build();
    }

    private void enrichUserProfileFromHr(UserProfileResponse.UserProfileResponseBuilder builder, Long userId) {
        try {
            EmployeeInternalResponse employee = hrClient.getEmployeeByUserId(userId);
            if (employee == null) {
                return;
            }

            builder.fullName(employee.getFullName())
                    .departmentName(employee.getDepartmentName())
                    .positionName(employee.getPositionName());
        } catch (FeignException.NotFound ex) {
            log.debug("Không có hồ sơ nhân viên liên kết với userId={}", userId);
        } catch (FeignException ex) {
            log.warn("Bỏ qua đồng bộ hồ sơ nhân viên cho userId={} vì hr-service chưa sẵn sàng: status={}", userId, ex.status());
        } catch (Exception ex) {
            log.warn("Bỏ qua đồng bộ hồ sơ nhân viên cho userId={} do lỗi ngoài dự kiến: {}", userId, ex.getMessage());
        }
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
        clearStoredRefreshToken(user);
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

    private java.util.Optional<User> findUserByLoginIdentifier(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .or(() -> userRepository.findFirstByUsernameIgnoreCase(identifier))
                .or(() -> userRepository.findFirstByEmailIgnoreCase(identifier));
    }

    private java.util.Optional<User> findOtherUserWithSameUsername(Long currentUserId, String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findFirstByUsernameIgnoreCase(username))
                .filter(found -> !found.getId().equals(currentUserId));
    }

    private java.util.Optional<User> findOtherUserWithSameEmail(Long currentUserId, String email) {
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findFirstByEmailIgnoreCase(email))
                .filter(found -> !found.getId().equals(currentUserId));
    }

    private User resolveUserFromAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }

        try {
            Claims claims = jwtService.parseClaims(accessToken);
            if (!"ACCESS".equals(claims.get("token_type", String.class))) {
                return null;
            }
            return resolveUserFromClaims(claims);
        } catch (ResponseStatusException | JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private User resolveUserFromStoredRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return null;
        }

        try {
            String normalizedToken = refreshToken.trim();
            Claims claims = jwtService.parseClaims(normalizedToken);
            if (!"REFRESH".equals(claims.get("token_type", String.class))) {
                return null;
            }

            User user = resolveUserFromClaims(claims);
            validateStoredRefreshToken(user, normalizedToken);
            return user;
        } catch (ResponseStatusException | JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private void validateStoredRefreshToken(User user, String refreshToken) {
        if (user.getRefreshTokenHash() == null || user.getRefreshTokenHash().isBlank() || user.getRefreshTokenExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token không còn hiệu lực");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (user.getRefreshTokenExpiresAt().isBefore(now)) {
            clearStoredRefreshToken(user);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token đã hết hạn");
        }

        if (!matchesStoredRefreshToken(user.getRefreshTokenHash(), refreshToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token không còn hiệu lực");
        }
    }

    private void persistRefreshToken(User user, String refreshToken) {
        user.setRefreshTokenHash(hashRefreshToken(refreshToken));
        user.setRefreshTokenExpiresAt(OffsetDateTime.ofInstant(jwtService.getExpiration(refreshToken), ZoneOffset.UTC));
        userRepository.save(user);
    }

    private void clearStoredRefreshToken(User user) {
        user.setRefreshTokenHash(null);
        user.setRefreshTokenExpiresAt(null);
        userRepository.save(user);
    }

    private boolean matchesStoredRefreshToken(String storedHash, String refreshToken) {
        byte[] expected = storedHash.getBytes(StandardCharsets.UTF_8);
        byte[] actual = hashRefreshToken(refreshToken).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 không khả dụng", ex);
        }
    }

    // Một user chỉ giữ một refresh token đang hoạt động tại một thời điểm.
    private AuthResponse issueTokensForUser(User user) {
        List<String> roleNames = user.getRoles().stream().map(Role::getRoleName).toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roleNames);
        claims.put("username", user.getUsername());

        String accessToken = jwtService.generateAccessToken(String.valueOf(user.getId()), claims);
        String refreshToken = jwtService.generateRefreshToken(
            String.valueOf(user.getId()),
            Map.of("username", user.getUsername())
        );

        persistRefreshToken(user, refreshToken);

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

    private InternalUserResponse toInternalUserResponse(User user) {
        return new InternalUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getRoleName).collect(java.util.stream.Collectors.toSet()));
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




