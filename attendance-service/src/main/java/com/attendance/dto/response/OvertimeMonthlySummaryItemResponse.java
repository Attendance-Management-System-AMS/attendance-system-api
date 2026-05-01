package com.attendance.dto.response;

public record OvertimeMonthlySummaryItemResponse(
        int month,
        String label,
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
