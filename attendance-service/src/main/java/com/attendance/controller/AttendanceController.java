package com.attendance.controller;

import com.attendance.entity.Attendance;
import com.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Chấm công - Check in/out", description = "Ghi nhận chấm công vào và ra")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in/{employeeId}")
    @Operation(summary = "Check-in nhân viên")
    public ResponseEntity<Attendance> checkIn(
            @Parameter(description = "ID nhân viên", example = "12")
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkIn(employeeId));
    }

    @PostMapping("/check-out/{employeeId}")
    @Operation(summary = "Check-out nhân viên")
    public ResponseEntity<Attendance> checkOut(
            @Parameter(description = "ID nhân viên", example = "12")
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.checkOut(employeeId));
    }
}
