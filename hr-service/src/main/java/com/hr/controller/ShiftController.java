package com.hr.controller;

import com.hr.dto.common.ApiResponse;
import com.hr.dto.shift.ShiftRequest;
import com.hr.dto.shift.ShiftResponse;
import com.hr.service.ShiftService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    public ApiResponse<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.create(request);
        return ApiResponse.success(201, "Tạo ca làm thành công", response);
    }

    @GetMapping
    public ApiResponse<List<ShiftResponse>> getShifts() {
        return ApiResponse.success(shiftService.getAll());
    }
}
