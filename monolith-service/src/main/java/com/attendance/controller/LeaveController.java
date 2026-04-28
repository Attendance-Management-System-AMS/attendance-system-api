package com.attendance.controller;

import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.response.PageResponse;
import com.attendance.dto.request.LeaveRequestRecord;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.exception.AppException;
import com.attendance.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Đơn từ & Nghỉ phép", description = "Quản lý đơn xin nghỉ và phê duyệt nghỉ phép")
public class LeaveController {

    private final LeaveService leaveService;
    private final com.attendance.service.EmployeeService employeeService;

    // Khởi tạo controller với service xử lý đơn nghỉ.
    public LeaveController(LeaveService leaveService, com.attendance.service.EmployeeService employeeService) {
        this.leaveService = leaveService;
        this.employeeService = employeeService;
    }

    // Lấy danh sách đơn nghỉ của tôi.
    @GetMapping("/me")
    @Operation(summary = "Đơn từ của tôi", description = "Lấy lịch sử đơn xin nghỉ của nhân viên đang đăng nhập")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<PageResponse<LeaveResponse>> getMyLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái đơn (không bắt buộc)")
            @RequestParam(required = false) String status) {
        Long employeeId = employeeService.getCurrentEmployeeId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success("Lấy đơn từ của tôi thành công", 
                leaveService.search(keyword, employeeId, status, pageable));
    }

    // Tạo đơn xin nghỉ cho chính tôi.
    @PostMapping("/me")
    @Operation(summary = "Tạo đơn xin nghỉ cho chính mình")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<LeaveResponse> createMyRequest(@Valid @RequestBody LeaveRequestRecord request) {
        Long employeeId = employeeService.getCurrentEmployeeId();
        // Ghi đè employeeId trong request bằng ID thực tế của user đang đăng nhập
        LeaveRequestRecord secureRequest = new LeaveRequestRecord(
                employeeId,
                request.leaveTypeCode(),
                request.fromDate(),
                request.toDate(),
                request.totalDays(),
                request.reason());
        LeaveResponse response = leaveService.createRequest(secureRequest);
        return ApiResponse.success(201, "Gửi đơn xin nghỉ thành công", response);
    }

    // Tạo mới đơn xin nghỉ cho nhân viên (Dành cho HR/Manager).
    @PostMapping
    @Operation(summary = "Tạo đơn xin nghỉ (HR/Manager)")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<LeaveResponse> createRequest(@Valid @RequestBody LeaveRequestRecord request) {
        LeaveResponse response = leaveService.createRequest(request);
        return ApiResponse.success(201, "Tạo đơn xin nghỉ thành công", response);
    }

    // Lấy danh sách đơn nghỉ theo bộ lọc và phân trang.
    @GetMapping
    @Operation(summary = "Lấy danh sách đơn nghỉ (phân trang, lọc)",
            description = "Lọc theo keyword (loại nghỉ, lý do, tên NV), employeeId, status: PENDING, APPROVED, REJECTED")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<PageResponse<LeaveResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Trạng thái đơn (không bắt buộc)")
            @RequestParam(required = false) String status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(leaveService.search(keyword, employeeId, status, pageable));
    }

    // Lấy chi tiết một đơn nghỉ theo ID.
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết đơn nghỉ")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<LeaveResponse> getById(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        return ApiResponse.success(leaveService.getById(id));
    }

    // Lấy danh sách các loại nghỉ đang dùng.
    @GetMapping("/types")
    @Operation(summary = "Lấy danh sách loại nghỉ có sẵn")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<List<LeaveTypeResponse>> getAllLeaveTypes() {
        return ApiResponse.success(leaveService.getAllLeaveTypes());
    }

    // Phê duyệt một đơn nghỉ đang chờ xử lý.
    @PutMapping("/{id}/approve")
    @Operation(summary = "Phê duyệt đơn nghỉ")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<LeaveResponse> approve(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        Long approvedById = null;
        try {
            approvedById = employeeService.getCurrentEmployeeId();
        } catch (AppException ignored) {
            // Tài khoản quản trị hệ thống có thể không gắn hồ sơ nhân viên.
        }
        return ApiResponse.success(200, "Phê duyệt đơn nghỉ thành công", leaveService.approve(id, approvedById));
    }

    // Từ chối một đơn nghỉ đang chờ xử lý.
    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối đơn nghỉ")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<LeaveResponse> reject(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        return ApiResponse.success(200, "Từ chối đơn nghỉ thành công", leaveService.reject(id));
    }

    // Huỷ một đơn nghỉ nếu vẫn còn ở trạng thái PENDING.
    @DeleteMapping("/{id}")
    @Operation(summary = "Huỷ đơn nghỉ", description = "Chỉ huỷ đơn đang ở trạng thái PENDING")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        leaveService.delete(id);
        return ApiResponse.success(200, "Huỷ đơn nghỉ thành công", null);
    }
}




