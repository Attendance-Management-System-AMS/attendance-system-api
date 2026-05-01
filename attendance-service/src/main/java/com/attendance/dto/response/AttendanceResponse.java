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
        Integer actualOvertimeMinutes,
        Integer approvedOvertimeMinutes,
        Integer payableOvertimeMinutes,
        String overtimeStatus,
        LocalDateTime createdAt,
        String employeeFullName,
        String employeeSnapshotCode,
        String employeeSnapshotDepartmentName,
        String employeeSnapshotPositionName
) {
    public AttendanceResponse(
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
            String employeeSnapshotPositionName) {
        this(
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
                0,
                0,
                0,
                "NONE",
                createdAt,
                employeeFullName,
                employeeSnapshotCode,
                employeeSnapshotDepartmentName,
                employeeSnapshotPositionName);
    }

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
                actualOvertimeMinutes,
                approvedOvertimeMinutes,
                payableOvertimeMinutes,
                overtimeStatus,
                createdAt,
                employeeFullName,
                employeeSnapshotCode,
                employeeSnapshotDepartmentName,
                employeeSnapshotPositionName);
    }
}


