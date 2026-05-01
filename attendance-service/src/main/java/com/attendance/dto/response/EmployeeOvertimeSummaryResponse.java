package com.attendance.dto.response;

public record EmployeeOvertimeSummaryResponse(
        Long employeeId,
        String employeeCode,
        String fullName,
        String departmentName,
        String positionName,
        int requestCount,
        int pendingRequests,
        int approvedRequests,
        int rejectedRequests,
        int cancelledRequests,
        int requestedMinutes,
        int approvedMinutes,
        int actualMinutes,
        int payableMinutes) {
}
