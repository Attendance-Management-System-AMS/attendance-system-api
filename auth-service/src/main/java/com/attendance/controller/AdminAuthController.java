package com.attendance.controller;

import com.attendance.common.dto.ApiResponse;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.AdminUpdateUserAccessRequest;
import com.attendance.dto.response.AdminUserResponse;
import com.attendance.dto.response.RoleSummaryResponse;
import com.attendance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Quản trị phân quyền", description = "Tra cứu vai trò và điều chỉnh quyền truy cập tài khoản")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminAuthController {

    private final AuthService authService;

    @GetMapping("/roles")
    @Operation(summary = "Lấy danh sách vai trò", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<List<RoleSummaryResponse>> getRoles() {
        return ApiResponse.success("Lấy danh sách vai trò thành công", authService.getRoleSummaries());
    }

    @GetMapping("/admin/users")
    @Operation(summary = "Lấy danh sách tài khoản để phân quyền", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<PageResponse<AdminUserResponse>> getAdminUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String role) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(
                "Lấy danh sách tài khoản thành công",
                authService.searchAdminUsers(keyword, enabled, role, pageable));
    }

    @PutMapping("/admin/users/{id}/access")
    @Operation(summary = "Cập nhật quyền truy cập tài khoản", security = {@SecurityRequirement(name = "BearerAuth")})
    public ApiResponse<AdminUserResponse> updateUserAccess(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserAccessRequest request) {
        return ApiResponse.success(
                "Cập nhật quyền truy cập thành công",
                authService.updateAdminUserAccess(id, request));
    }
}
