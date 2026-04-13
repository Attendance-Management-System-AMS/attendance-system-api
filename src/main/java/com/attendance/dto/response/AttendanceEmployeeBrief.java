package com.attendance.dto.response;

/**
 * Thông tin nhân viên kèm bản ghi chấm công (ví dụ sau check-in bằng mặt) để kiosk không cần gọi thêm API HR.
 */
public record AttendanceEmployeeBrief(
        String fullName,
        String employeeCode,
        String departmentName,
        String positionName
) {}


