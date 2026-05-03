package com.attendance.dto.request;

import jakarta.validation.constraints.Size;

public record OvertimeApprovalRequest(
        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
        String note
) {
}
