package com.attendance.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AttendanceAnnualSummaryResponse(
        int year,
        LocalDate fromDate,
        LocalDate toDate,
        Long departmentId,
        Long employeeId,
        int totalEmployees,
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
        List<AttendanceMonthlySummaryItemResponse> months,
        List<EmployeeAttendanceSummaryResponse> employees) {
}
