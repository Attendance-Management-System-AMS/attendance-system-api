package com.attendance.dto.response;

public record AttendanceMonthlySummaryItemResponse(
        int month,
        String label,
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
