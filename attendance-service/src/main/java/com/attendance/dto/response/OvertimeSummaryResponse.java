package com.attendance.dto.response;

import java.time.LocalDate;
import java.util.List;

public record OvertimeSummaryResponse(
        int year,
        LocalDate fromDate,
        LocalDate toDate,
        Long departmentId,
        Long employeeId,
        int totalEmployees,
        int requestCount,
        int pendingRequests,
        int approvedRequests,
        int rejectedRequests,
        int cancelledRequests,
        int requestedMinutes,
        int approvedMinutes,
        int actualMinutes,
        int payableMinutes,
        List<OvertimeMonthlySummaryItemResponse> months,
        List<EmployeeOvertimeSummaryResponse> employees) {
}
