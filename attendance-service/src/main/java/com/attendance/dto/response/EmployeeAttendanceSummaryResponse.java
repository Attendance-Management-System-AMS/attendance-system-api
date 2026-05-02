package com.attendance.dto.response;

import java.util.List;

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
        int overtimeMinutes,
        List<AttendanceMonthlySummaryItemResponse> months) {
}
