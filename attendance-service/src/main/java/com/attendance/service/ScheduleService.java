package com.attendance.service;

import com.common.exception.AppException;
import com.attendance.dto.schedule.EmployeeScheduleRequest;
import com.attendance.dto.schedule.EmployeeScheduleResponse;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.Shift;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.EmployeeScheduleMapper;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.EmployeeScheduleSpecifications;
import com.attendance.repository.ShiftRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeScheduleMapper employeeScheduleMapper;

    // Khởi tạo service xử lý lịch làm việc.
    public ScheduleService(EmployeeScheduleRepository employeeScheduleRepository,
                           ShiftRepository shiftRepository,
                           EmployeeScheduleMapper employeeScheduleMapper) {
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.shiftRepository = shiftRepository;
        this.employeeScheduleMapper = employeeScheduleMapper;
    }

    // Gán ca làm cho nhân viên.
    @Transactional
    public EmployeeScheduleResponse assignSchedule(EmployeeScheduleRequest request) {
        Shift shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setEmployeeId(request.employeeId());
        schedule.setShift(shift);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setIsActive(request.isActive());
        schedule.setEffectiveFrom(request.effectiveFrom());

        EmployeeSchedule saved = employeeScheduleRepository.save(schedule);
        return employeeScheduleMapper.toResponse(saved);
    }

    // Lấy toàn bộ lịch làm của nhân viên.
    @Transactional(readOnly = true)
    public List<EmployeeScheduleResponse> getByEmployee(Long employeeId) {
        return employeeScheduleRepository.findByEmployeeId(employeeId)
                .stream()
                .map(employeeScheduleMapper::toResponse)
                .toList();
    }

    // Tìm kiếm lịch làm theo bộ lọc và phân trang.
    @Transactional(readOnly = true)
    public Page<EmployeeScheduleResponse> search(
            Long employeeId,
            Integer dayOfWeek,
            Boolean isActive,
            LocalDate effectiveFromOnOrBefore,
            Long shiftId,
            Pageable pageable) {
        return employeeScheduleRepository
                .findAll(
                        EmployeeScheduleSpecifications.matches(
                                employeeId, dayOfWeek, isActive, effectiveFromOnOrBefore, shiftId),
                        pageable)
                .map(employeeScheduleMapper::toResponse);
    }

    // Xóa lịch làm theo ID.
    @Transactional
    public void delete(Long id) {
        EmployeeSchedule schedule = employeeScheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        employeeScheduleRepository.delete(schedule);
    }
}
