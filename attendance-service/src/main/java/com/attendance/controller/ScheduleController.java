package com.attendance.controller;

import com.common.dto.ApiResponse;
import com.attendance.dto.schedule.EmployeeScheduleRequest;
import com.attendance.dto.schedule.EmployeeScheduleResponse;
import com.attendance.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
@Tag(name = "Chấm công - Lịch làm việc", description = "Gán lịch làm việc cho nhân viên")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "Gán lịch làm việc cho nhân viên")
    public ApiResponse<EmployeeScheduleResponse> assignSchedule(@Valid @RequestBody EmployeeScheduleRequest request) {
        EmployeeScheduleResponse response = scheduleService.assignSchedule(request);
        return ApiResponse.success("Gán ca làm việc thành công", response);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lấy lịch làm việc của nhân viên")
    public ApiResponse<List<EmployeeScheduleResponse>> getByEmployee(
            @Parameter(description = "ID nhân viên") @PathVariable Long employeeId) {
        return ApiResponse.success("Lấy lịch làm việc thành công", scheduleService.getByEmployee(employeeId));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm lịch làm (filter + paging)")
    public ApiResponse<Page<EmployeeScheduleResponse>> search(
            @Parameter(description = "ID nhân viên")
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @Parameter(description = "Thứ trong tuần (1-7)")
            @RequestParam(value = "dayOfWeek", required = false) Integer dayOfWeek,
            @Parameter(description = "Đang hoạt động hay không")
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @Parameter(description = "Ngày hiệu lực <= (yyyy-MM-dd)")
            @RequestParam(value = "effectiveFromOnOrBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFromOnOrBefore,
            @Parameter(description = "ID ca làm")
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @PageableDefault(size = 20, sort = "effectiveFrom", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(
                "Tìm kiếm lịch làm thành công",
                scheduleService.search(employeeId, dayOfWeek, isActive, effectiveFromOnOrBefore, shiftId, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa lịch làm việc")
    public ApiResponse<Void> deleteSchedule(
            @Parameter(description = "ID lịch làm") @PathVariable Long id) {
        scheduleService.delete(id);
        return ApiResponse.success("Xóa lịch làm việc thành công", null);
    }
}
