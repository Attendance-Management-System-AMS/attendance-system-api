package com.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        Long employeeId,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        LocalDate workDate,
        String status,
        Integer lateMinutes,
        Integer earlyLeaveMinutes,
        Integer workedMinutes,
        Integer expectedMinutes,
        LocalDateTime createdAt,
        String employeeFullName,
        String employeeSnapshotCode,
        String employeeSnapshotDepartmentName,
        String employeeSnapshotPositionName
) {
    public AttendanceResponse withStatus(String status) {
        return new AttendanceResponse(
                id,
                employeeId,
                checkInTime,
                checkOutTime,
                workDate,
                status,
                lateMinutes,
                earlyLeaveMinutes,
                workedMinutes,
                expectedMinutes,
                createdAt,
                employeeFullName,
                employeeSnapshotCode,
                employeeSnapshotDepartmentName,
                employeeSnapshotPositionName);
    }
}


