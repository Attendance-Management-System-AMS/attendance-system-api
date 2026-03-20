package com.attendance.controller;

import com.common.dto.ApiResponse;
import com.attendance.dto.holiday.HolidayRequest;
import com.attendance.dto.holiday.HolidayResponse;
import com.attendance.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/holidays")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Ngày nghỉ", description = "Quản lý ngày nghỉ lễ và nghỉ hưởng lương")
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    @Operation(summary = "Lấy danh sách ngày nghỉ")
    public ApiResponse<List<HolidayResponse>> getHolidays() {
        return ApiResponse.success("Lấy danh sách ngày nghỉ thành công", holidayService.getAll());
    }

    @PostMapping
    @Operation(summary = "Tạo ngày nghỉ")
    public ApiResponse<HolidayResponse> createHoliday(@Valid @RequestBody HolidayRequest request) {
        HolidayResponse response = holidayService.create(request);
        return ApiResponse.success("Tạo ngày nghỉ thành công", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết ngày nghỉ theo ID")
    public ApiResponse<HolidayResponse> getHolidayById(
            @Parameter(description = "ID ngày nghỉ") @PathVariable Long id) {
        return ApiResponse.success("Lấy ngày nghỉ thành công", holidayService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật ngày nghỉ")
    public ApiResponse<HolidayResponse> updateHoliday(
            @Parameter(description = "ID ngày nghỉ") @PathVariable Long id,
            @Valid @RequestBody HolidayRequest request) {
        return ApiResponse.success("Cập nhật ngày nghỉ thành công", holidayService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ngày nghỉ")
    public ApiResponse<Void> deleteHoliday(
            @Parameter(description = "ID ngày nghỉ") @PathVariable Long id) {
        holidayService.delete(id);
        return ApiResponse.success("Xóa ngày nghỉ thành công", null);
    }
}
