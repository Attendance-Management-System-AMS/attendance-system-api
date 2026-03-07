package com.hr.controller;

import com.hr.dto.common.ApiResponse;
import com.hr.dto.schedule.EmployeeScheduleRequest;
import com.hr.dto.schedule.EmployeeScheduleResponse;
import com.hr.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr/schedules")
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
