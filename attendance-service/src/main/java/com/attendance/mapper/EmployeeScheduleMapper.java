package com.attendance.mapper;

import com.attendance.dto.schedule.EmployeeScheduleResponse;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import org.springframework.stereotype.Component;

@Component
public class EmployeeScheduleMapper {

    // Chuyển lịch làm sang response hiển thị.
    public EmployeeScheduleResponse toResponse(EmployeeSchedule schedule) {
        Shift shift = schedule.getShift();

        return new EmployeeScheduleResponse(
                schedule.getId(),
                schedule.getEmployeeId(),
                null, // employeeName is not available directly in attendance-service
                shift != null ? shift.getId() : null,
                shift != null ? shift.getName() : null,
                schedule.getDayOfWeek(),
                schedule.getIsActive(),
                schedule.getEffectiveFrom()
        );
    }
}
