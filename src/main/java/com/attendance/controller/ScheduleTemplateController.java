package com.attendance.controller;

import com.attendance.dto.response.ApiResponse;
import com.attendance.dto.request.ScheduleTemplateRequest;
import com.attendance.dto.response.ScheduleTemplateResponse;
import com.attendance.service.ScheduleTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance/schedule-templates")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Mẫu lịch làm việc", description = "Quản lý mẫu lịch tuần để gán cho nhân viên")
public class ScheduleTemplateController {

    private final ScheduleTemplateService templateService;

    @PostMapping
    @Operation(summary = "Tạo mẫu lịch mới")
    public ApiResponse<ScheduleTemplateResponse> create(@Valid @RequestBody ScheduleTemplateRequest request) {
        return ApiResponse.success("Tạo mẫu lịch thành công", templateService.create(request));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả mẫu lịch")
    public ApiResponse<List<ScheduleTemplateResponse>> getAll() {
        return ApiResponse.success("Lấy danh sách mẫu lịch thành công", templateService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết mẫu lịch")
    public ApiResponse<ScheduleTemplateResponse> getById(@PathVariable Long id) {
        return ApiResponse.success("Lấy chi tiết mẫu lịch thành công", templateService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật mẫu lịch")
    public ApiResponse<ScheduleTemplateResponse> update(
            @PathVariable Long id, 
            @Valid @RequestBody ScheduleTemplateRequest request) {
        return ApiResponse.success("Cập nhật mẫu lịch thành công", templateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa mẫu lịch")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ApiResponse.success("Xóa mẫu lịch thành công", null);
    }
}



