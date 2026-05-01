package com.attendance.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record OvertimeRequestResponse(
        Long id,
        Long employeeId,
        String employeeFullName,
        String employeeCode,
        String departmentName,
        String positionName,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer requestedMinutes,
        String reason,
        String status,
        Long approvedById,
        String approvedByName,
        LocalDateTime approvedAt,
        String approvalNote,
        Boolean hasAttendance,
        String attendanceStatus,
        Integer actualOvertimeMinutes,
        Integer approvedOvertimeMinutes,
        Integer payableOvertimeMinutes,
        String attendanceOvertimeStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
