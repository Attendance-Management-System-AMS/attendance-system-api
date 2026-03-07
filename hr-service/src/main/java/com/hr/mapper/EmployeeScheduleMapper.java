package com.hr.mapper;

import com.hr.dto.schedule.EmployeeScheduleResponse;
import com.hr.entity.Employee;
import com.hr.entity.EmployeeSchedule;
import com.hr.entity.Shift;
import org.springframework.stereotype.Component;

@Component
public class EmployeeScheduleMapper {

    public EmployeeScheduleResponse toResponse(EmployeeSchedule schedule) {
        Employee employee = schedule.getEmployee();
        Shift shift = schedule.getShift();

        return new EmployeeScheduleResponse(
                schedule.getId(),
                employee != null ? employee.getId() : null,
                employee != null ? employee.getFullName() : null,
                shift != null ? shift.getId() : null,
                shift != null ? shift.getName() : null,
                schedule.getDayOfWeek(),
                schedule.getIsActive(),
                schedule.getEffectiveFrom()
        );
    }
}
