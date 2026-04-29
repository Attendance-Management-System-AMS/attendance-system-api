package com.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public record LeaveRequestRecord(
        Long employeeId,

        @NotBlank(message = "Loại nghỉ là bắt buộc")
        @Size(max = 10, message = "Mã loại nghỉ tối đa 10 ký tự")
        String leaveTypeCode,

        @NotNull(message = "Ngày bắt đầu nghỉ là bắt buộc")
        LocalDate fromDate,

        @NotNull(message = "Ngày kết thúc nghỉ là bắt buộc")
        LocalDate toDate,

        Double totalDays,

        @Size(max = 500, message = "Lý do tối đa 500 ký tự")
        String reason,

        // Dùng cho đơn giải trình công (loại AC)
        LocalTime correctedCheckIn,
        LocalTime correctedCheckOut
) {
}

