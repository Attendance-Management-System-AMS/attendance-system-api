package com.attendance.controller;

import com.attendance.dto.common.ApiResponse;
import com.attendance.dto.schedule.EmployeeScheduleRequest;
import com.attendance.dto.schedule.EmployeeScheduleResponse;
import com.attendance.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ApiResponse<EmployeeScheduleResponse> assignSchedule(@Valid @RequestBody EmployeeScheduleRequest request) {
        EmployeeScheduleResponse response = scheduleService.assignSchedule(request);
        return ApiResponse.success(201, "Gán ca làm việc thành công", response);
    }
}
