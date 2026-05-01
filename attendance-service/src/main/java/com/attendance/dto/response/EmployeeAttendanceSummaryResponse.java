package com.attendance.dto.response;

public record EmployeeAttendanceSummaryResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        String departmentName,
        String positionName,
        int workDays,
        int lateDays,
        int earlyLeaveDays,
        int absentDays,
        int leaveDays,
        int holidayDays,
        int missingCheckoutDays,
        int incompleteDays,
        int workedMinutes,
        int overtimeMinutes) {
}
