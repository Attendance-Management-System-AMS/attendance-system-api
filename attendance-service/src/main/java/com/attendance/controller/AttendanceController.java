package com.attendance.controller;

import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.dto.response.KioskSessionResponse;
import com.attendance.common.dto.ApiResponse;
import com.attendance.common.dto.PageResponse;
import com.attendance.service.CurrentUserService;
import com.attendance.service.AttendanceService;
import com.attendance.service.KioskAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Chấm công & Bảng công", description = "Quản lý ghi nhận check-in/out và tra cứu lịch sử bảng công nhân viên")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUserService currentUserService;
    private final KioskAccessService kioskAccessService;

    // Ghi nhận check-in cho nhân viên theo ID.
    @PostMapping("/check-in/{employeeId}")
    @Operation(summary = "Check-in nhân viên")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<AttendanceResponse> checkIn(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        return ApiResponse.success("Check-in thành công", attendanceService.checkIn(employeeId));
    }

    @PostMapping("/kiosk/session")
    @Operation(summary = "Cấp phiên kiosk ngắn hạn cho máy chấm công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<KioskSessionResponse> issueKioskSession(
            @RequestHeader(value = KioskAccessService.HEADER_DEVICE_ID, required = false) String deviceId,
            Authentication authentication) {
        return ApiResponse.success(
                "Cấp phiên kiosk thành công",
                kioskAccessService.issueSession(authentication == null ? null : authentication.getName(), deviceId));
    }

    @PostMapping("/scan-by-face")
    @Operation(summary = "Quét khuôn mặt để tự động check-in hoặc check-out")
    @PreAuthorize("permitAll()")
    public ApiResponse<AttendanceResponse> scanByFace(
            @Valid @RequestBody FaceDescriptorRequest request,
            @RequestHeader(value = KioskAccessService.HEADER_SESSION, required = false) String kioskSession,
            @RequestHeader(value = KioskAccessService.HEADER_DEVICE_ID, required = false) String deviceId,
            @RequestHeader(value = KioskAccessService.HEADER_NONCE, required = false) String nonce,
            @RequestHeader(value = KioskAccessService.HEADER_TIMESTAMP, required = false) String timestamp) {
        String validatedDeviceId = kioskAccessService.validateScanRequest(kioskSession, deviceId, nonce, timestamp);
        return ApiResponse.success("Chấm công thành công", attendanceService.scanByFace(request, validatedDeviceId));
    }

    // Ghi nhận check-out cho nhân viên theo ID.
    @PostMapping("/check-out/{employeeId}")
    @Operation(summary = "Check-out nhân viên")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<AttendanceResponse> checkOut(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        return ApiResponse.success("Check-out thành công", attendanceService.checkOut(employeeId));
    }

    // Lấy bảng công của tôi (người dùng đang đăng nhập).
    @GetMapping("/me")
    @Operation(summary = "Bảng công của tôi", description = "Lấy lịch sử chấm công của nhân viên đang đăng nhập")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<PageResponse<AttendanceResponse>> getMyAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "workDate") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Ngày chấm công chính xác (yyyy-MM-dd)") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Từ ngày (yyyy-MM-dd)") @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)") @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Trạng thái") @RequestParam(value = "status", required = false) String status) {
        Long employeeId = currentUserService.getCurrentEmployeeId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(
                "Lấy bảng công của tôi thành công",
                attendanceService.search(employeeId, date, fromDate, toDate, status, pageable));
    }

    // Lấy chấm công hôm nay của tôi.
    @GetMapping("/today/me")
    @Operation(summary = "Chấm công hôm nay của tôi")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<AttendanceResponse> getMyTodayAttendance() {
        Long employeeId = currentUserService.getCurrentEmployeeId();
        return ApiResponse.success("Lấy chấm công hôm nay thành công",
                attendanceService.getTodayByEmployee(employeeId));
    }

    // Lấy bản ghi chấm công của nhân viên trong ngày hôm nay.
    @GetMapping("/employee/{employeeId}/today")
    @Operation(summary = "Chấm công hôm nay của nhân viên", description = "Vẫn giữ lại để hỗ trợ check nhanh cho admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<AttendanceResponse> getTodayAttendance(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        return ApiResponse.success("Lấy chấm công hôm nay thành công",
                attendanceService.getTodayByEmployee(employeeId));
    }

    // Tìm kiếm chấm công theo bộ lọc và phân trang.
    @GetMapping
    @Operation(summary = "Tìm kiếm chấm công (phân trang, lọc)")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<PageResponse<AttendanceResponse>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "workDate") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "ID nhân viên") @RequestParam(value = "employeeId", required = false) Long employeeId,
            @Parameter(description = "Ngày chấm công chính xác (yyyy-MM-dd). Nếu có date thì from/to bị bỏ qua.") @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Từ ngày (yyyy-MM-dd)") @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Đến ngày (yyyy-MM-dd)") @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Trạng thái: PRESENT/LATE/EARLY_LEAVE/ABSENT/...") @RequestParam(value = "status", required = false) String status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(
                "Tìm kiếm chấm công thành công",
                attendanceService.search(employeeId, date, fromDate, toDate, status, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa bản ghi chấm công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ApiResponse.success(200, "Xóa bản ghi chấm công thành công", null);
    }
}
