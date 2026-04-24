package com.attendance.controller;

import com.attendance.common.dto.ApiResponse;
import com.attendance.dto.request.ApplyTemplateRequest;
import com.attendance.dto.request.BulkScheduleRequest;
import com.attendance.dto.request.EmployeeScheduleRequest;
import com.attendance.dto.response.EmployeeScheduleResponse;
import com.attendance.service.CurrentUserService;
import com.attendance.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.attendance.common.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/schedules")
@RequiredArgsConstructor
@Tag(name = "Lịch làm việc", description = "Quản lý phân ca và gán lịch làm việc cho nhân viên")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final CurrentUserService currentUserService;

    // Lấy lịch làm việc của tôi.
    @GetMapping("/me")
    @Operation(summary = "Lịch làm việc của tôi", description = "Lấy toàn bộ lịch làm việc của nhân viên đang đăng nhập")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER','ROLE_EMPLOYEE')")
    public ApiResponse<List<EmployeeScheduleResponse>> getMySchedule() {
        Long employeeId = currentUserService.getCurrentEmployeeId();
        return ApiResponse.success("Lấy lịch làm việc của tôi thành công", 
                scheduleService.getByEmployee(employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lịch làm việc của nhân viên")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<List<EmployeeScheduleResponse>> getByEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        return ApiResponse.success("Lấy lịch làm việc của nhân viên thành công",
                scheduleService.getByEmployee(employeeId));
    }

    // Gán lịch làm việc cho nhân viên.
    @PostMapping
    @Operation(summary = "Gán lịch làm việc cho nhân viên")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<EmployeeScheduleResponse> assignSchedule(@Valid @RequestBody EmployeeScheduleRequest request) {
        EmployeeScheduleResponse response = scheduleService.assignSchedule(request);
        return ApiResponse.success("Gán ca làm việc thành công", response);
    }

    // Cập nhật ca làm của một lịch đã phân.
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    @Operation(summary = "Cập nhật ca làm của một lịch đã phân (Atomic update)")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<EmployeeScheduleResponse> updateSchedule(
            @Parameter(description = "ID lịch làm") @PathVariable Long id,
            @Valid @RequestBody com.attendance.dto.request.UpdateScheduleRequest request) {
        EmployeeScheduleResponse response = scheduleService.updateSchedule(id, request);
        return ApiResponse.success("Cập nhật lịch làm việc thành công", response);
    }

    // Gán lịch làm hàng loạt.
    @PostMapping("/bulk")
    @Operation(summary = "Gán ca làm hàng loạt cho nhiều người, nhiều ngày")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<List<EmployeeScheduleResponse>> bulkAssign(@Valid @RequestBody BulkScheduleRequest request) {
        return ApiResponse.success("Gán ca làm hàng loạt thành công", scheduleService.bulkAssign(request));
    }

    // Áp dụng mẫu lịch.
    @PostMapping("/apply-template")
    @Operation(summary = "Áp dụng mẫu lịch (template) cho nhân viên")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<List<EmployeeScheduleResponse>> applyTemplate(@Valid @RequestBody ApplyTemplateRequest request) {
        return ApiResponse.success("Áp dụng mẫu lịch thành công", scheduleService.applyTemplate(request));
    }

    // Tìm kiếm lịch làm theo bộ lọc và phân trang.
    @GetMapping()
    @Operation(summary = "Tìm kiếm lịch làm (filter + paging)")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR','ROLE_MANAGER')")
    public ApiResponse<PageResponse<EmployeeScheduleResponse>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "effectiveFrom") String sort,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "ID nhân viên") @RequestParam(value = "employeeId", required = false) Long employeeId,
            @Parameter(description = "Thứ trong tuần (1-7)") @RequestParam(value = "dayOfWeek", required = false) Integer dayOfWeek,
            @Parameter(description = "Đang hoạt động hay không") @RequestParam(value = "isActive", required = false) Boolean isActive,
            @Parameter(description = "Ngày hiệu lực <= (yyyy-MM-dd)") @RequestParam(value = "effectiveFromOnOrBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFromOnOrBefore,
            @Parameter(description = "ID ca làm") @RequestParam(value = "shiftId", required = false) Long shiftId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sort));
        return ApiResponse.success(
                "Tìm kiếm lịch làm thành công",
                scheduleService.search(employeeId, dayOfWeek, isActive, effectiveFromOnOrBefore, shiftId, pageable));
    }

    // Xóa lịch làm việc theo ID.
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa lịch làm việc")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ApiResponse<Void> deleteSchedule(
            @Parameter(description = "ID lịch làm") @PathVariable Long id) {
        scheduleService.delete(id);
        return ApiResponse.success("Xóa lịch làm việc thành công", null);
    }
}

