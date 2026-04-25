package com.attendance.controller;

import com.attendance.service.AttendanceService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/attendance")
@RequiredArgsConstructor
public class InternalAttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/leave-approvals/sync")
    public void syncApprovedLeave(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        attendanceService.syncApprovedLeave(employeeId, fromDate, toDate);
    }
}
