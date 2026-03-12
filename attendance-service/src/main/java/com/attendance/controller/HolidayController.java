package com.attendance.controller;

import com.common.dto.ApiResponse;
import com.attendance.dto.holiday.HolidayRequest;
import com.attendance.dto.holiday.HolidayResponse;
import com.attendance.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/holidays")
@Tag(name = "Chấm công - Ngày nghỉ", description = "Quản lý ngày nghỉ lễ và nghỉ hưởng lương")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách ngày nghỉ")
    public ApiResponse<List<HolidayResponse>> getHolidays() {
        return ApiResponse.success(holidayService.getAll());
    }

    @PostMapping
    @Operation(summary = "Tạo ngày nghỉ")
    public ApiResponse<HolidayResponse> createHoliday(@Valid @RequestBody HolidayRequest request) {
        HolidayResponse response = holidayService.create(request);
        return ApiResponse.success(201, "Tạo ngày nghỉ thành công", response);
    }
}
