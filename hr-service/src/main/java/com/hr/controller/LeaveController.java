package com.hr.controller;

import com.common.dto.ApiResponse;
import com.common.pagination.PageResponse;
import com.hr.dto.leave.LeaveRequestRecord;
import com.hr.dto.leave.LeaveResponse;
import com.hr.dto.leave.LeaveTypeResponse;
import com.hr.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@Tag(name = "HR - Nghỉ phép", description = "Quản lý đơn nghỉ phép của nhân viên")
public class LeaveController {

    private final LeaveService leaveService;

    // Khởi tạo controller với service xử lý đơn nghỉ.
    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Tạo mới đơn xin nghỉ cho nhân viên.
    @PostMapping
    @Operation(summary = "Tạo đơn xin nghỉ")
    public ApiResponse<LeaveResponse> createRequest(@Valid @RequestBody LeaveRequestRecord request) {
        LeaveResponse response = leaveService.createRequest(request);
        return ApiResponse.success(201, "Gửi đơn xin nghỉ thành công", response);
    }

    // Lấy danh sách đơn nghỉ theo bộ lọc và phân trang.
    @GetMapping
    @Operation(summary = "Lấy danh sách đơn nghỉ (phân trang, lọc)",
            description = "Lọc theo keyword (loại nghỉ, lý do, tên NV), employeeId, status: PENDING, APPROVED, REJECTED")
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
    public ApiResponse<LeaveResponse> getById(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        return ApiResponse.success(leaveService.getById(id));
    }

    // Lấy danh sách đơn nghỉ của riêng một nhân viên.
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lấy danh sách đơn nghỉ theo nhân viên (phân trang)")
    public ApiResponse<PageResponse<LeaveResponse>> getByEmployee(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(leaveService.searchByEmployee(employeeId, pageable));
    }

    // Lấy danh sách các loại nghỉ đang dùng.
    @GetMapping("/types")
    @Operation(summary = "Lấy danh sách loại nghỉ có sẵn")
    public ApiResponse<List<LeaveTypeResponse>> getAllLeaveTypes() {
        return ApiResponse.success(leaveService.getAllLeaveTypes());
    }

    // Phê duyệt một đơn nghỉ đang chờ xử lý.
    @PutMapping("/{id}/approve")
    @Operation(summary = "Phê duyệt đơn nghỉ")
    public ApiResponse<LeaveResponse> approve(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id,
            @Parameter(description = "ID người phê duyệt (không bắt buộc)")
            @RequestParam(required = false) Long approvedById) {
        return ApiResponse.success(200, "Phê duyệt đơn nghỉ thành công", leaveService.approve(id, approvedById));
    }

    // Từ chối một đơn nghỉ đang chờ xử lý.
    @PutMapping("/{id}/reject")
    @Operation(summary = "Từ chối đơn nghỉ")
    public ApiResponse<LeaveResponse> reject(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        return ApiResponse.success(200, "Từ chối đơn nghỉ thành công", leaveService.reject(id));
    }

    // Huỷ một đơn nghỉ nếu vẫn còn ở trạng thái PENDING.
    @DeleteMapping("/{id}")
    @Operation(summary = "Huỷ đơn nghỉ", description = "Chỉ huỷ đơn đang ở trạng thái PENDING")
    public ApiResponse<Void> delete(
            @Parameter(description = "ID đơn nghỉ") @PathVariable Long id) {
        leaveService.delete(id);
        return ApiResponse.success(200, "Huỷ đơn nghỉ thành công", null);
    }
}

