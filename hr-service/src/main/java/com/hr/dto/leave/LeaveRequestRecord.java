package com.hr.dto.leave;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LeaveRequestRecord(
        @NotNull(message = "Mã nhân viên là bắt buộc")
        Long employeeId,

        @NotBlank(message = "Loại nghỉ là bắt buộc")
        String leaveType,

        @NotNull(message = "Ngày bắt đầu là bắt buộc")
        LocalDate fromDate,

        @NotNull(message = "Ngày kết thúc là bắt buộc")
        LocalDate toDate,

        String reason
) {
}
