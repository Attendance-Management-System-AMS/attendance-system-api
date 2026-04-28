package com.attendance.controller;

import com.attendance.service.LeaveService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/hr/leaves")
@RequiredArgsConstructor
public class InternalLeaveController {

    private final LeaveService leaveService;

    @GetMapping("/approved")
    public boolean hasApprovedLeave(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return leaveService.hasApprovedLeave(employeeId, date);
    }
}
