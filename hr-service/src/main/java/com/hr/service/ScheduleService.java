package com.hr.service;

import com.hr.dto.schedule.EmployeeScheduleRequest;
import com.hr.dto.schedule.EmployeeScheduleResponse;
import com.hr.entity.Employee;
import com.hr.entity.EmployeeSchedule;
import com.hr.entity.Shift;
import com.hr.exception.AppException;
import com.hr.exception.ErrorCode;
import com.hr.mapper.EmployeeScheduleMapper;
import com.hr.repository.EmployeeRepository;
import com.hr.repository.EmployeeScheduleRepository;
import com.hr.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeScheduleMapper employeeScheduleMapper;

    public ScheduleService(EmployeeScheduleRepository employeeScheduleRepository,
                           EmployeeRepository employeeRepository,
                           ShiftRepository shiftRepository,
                           EmployeeScheduleMapper employeeScheduleMapper) {
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.employeeScheduleMapper = employeeScheduleMapper;
    }

    @Transactional
    public EmployeeScheduleResponse assignSchedule(EmployeeScheduleRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Shift shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setEmployee(employee);
        schedule.setShift(shift);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setIsActive(request.isActive());
        schedule.setEffectiveFrom(request.effectiveFrom());

        EmployeeSchedule saved = employeeScheduleRepository.save(schedule);
        return employeeScheduleMapper.toResponse(saved);
    }
}
