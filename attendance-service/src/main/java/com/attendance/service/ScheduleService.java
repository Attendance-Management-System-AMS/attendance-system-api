package com.attendance.service;

import com.attendance.exception.AppException;
import com.attendance.dto.request.ApplyTemplateRequest;
import com.attendance.dto.request.BulkScheduleRequest;
import com.attendance.dto.request.EmployeeScheduleRequest;
import com.attendance.dto.response.EmployeeScheduleResponse;
import com.attendance.dto.response.ScheduleConflictDetail;
import com.attendance.common.dto.PageResponse;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.ScheduleTemplate;
import com.attendance.entity.ScheduleTemplateItem;
import com.attendance.entity.Shift;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.EmployeeScheduleMapper;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.spec.EmployeeScheduleSpecifications;
import com.attendance.repository.ScheduleTemplateRepository;
import com.attendance.repository.ShiftRepository;
import com.attendance.util.ShiftUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final ShiftRepository shiftRepository;
    private final ScheduleTemplateRepository templateRepository;
    private final EmployeeScheduleMapper employeeScheduleMapper;

    // Gán ca làm cho nhân viên đơn lẻ (Có kiểm tra xung đột).
    @Transactional
    public EmployeeScheduleResponse assignSchedule(EmployeeScheduleRequest request) {
        Shift newShift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        List<ScheduleConflictDetail> conflicts = new ArrayList<>();
        boolean force = request.force() != null && request.force();
        processConflicts(request.employeeId(), newShift, request.dayOfWeek(), force, conflicts);

        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Phát hiện xung đột lịch làm", conflicts);
        }

        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setEmployeeId(request.employeeId());
        schedule.setShift(newShift);
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setIsActive(request.isActive());
        schedule.setEffectiveFrom(request.effectiveFrom());

        EmployeeSchedule saved = employeeScheduleRepository.save(schedule);
        return employeeScheduleMapper.toResponse(saved);
    }

    // Gán ca làm hàng loạt cho nhiều nhân viên và nhiều ngày.
    @Transactional
    public List<EmployeeScheduleResponse> bulkAssign(BulkScheduleRequest request) {
        Shift shift = shiftRepository.findById(request.shiftId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        boolean force = request.force() != null && request.force();
        List<ScheduleConflictDetail> conflicts = new ArrayList<>();
        List<EmployeeSchedule> schedulesToSave = new ArrayList<>();

        for (Long employeeId : request.employeeIds()) {
            for (Integer dayOfWeek : request.daysOfWeek()) {
                processConflicts(employeeId, shift, dayOfWeek, force, conflicts);
                
                EmployeeSchedule schedule = new EmployeeSchedule();
                schedule.setEmployeeId(employeeId);
                schedule.setShift(shift);
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setIsActive(true);
                schedule.setEffectiveFrom(request.effectiveFrom());
                
                schedulesToSave.add(schedule);
            }
        }

        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Phát hiện xung đột lịch làm việc", conflicts);
        }

        return employeeScheduleRepository.saveAll(schedulesToSave)
                .stream()
                .map(employeeScheduleMapper::toResponse)
                .toList();
    }

    // Áp dụng mẫu lịch làm vào nhân viên.
    @Transactional
    public List<EmployeeScheduleResponse> applyTemplate(ApplyTemplateRequest request) {
        ScheduleTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND));

        if (template.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mẫu lịch này chưa có ca làm nào");
        }

        boolean force = request.force() != null && request.force();
        List<ScheduleConflictDetail> conflicts = new ArrayList<>();
        List<EmployeeSchedule> schedulesToSave = new ArrayList<>();

        for (Long employeeId : request.employeeIds()) {
            for (ScheduleTemplateItem item : template.getItems()) {
                processConflicts(employeeId, item.getShift(), item.getDayOfWeek(), force, conflicts);

                EmployeeSchedule schedule = new EmployeeSchedule();
                schedule.setEmployeeId(employeeId);
                schedule.setShift(item.getShift());
                schedule.setDayOfWeek(item.getDayOfWeek());
                schedule.setIsActive(true);
                schedule.setEffectiveFrom(request.effectiveFrom());
                
                schedulesToSave.add(schedule);
            }
        }

        if (!conflicts.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Phát hiện xung đột lịch làm việc", conflicts);
        }

        return employeeScheduleRepository.saveAll(schedulesToSave)
                .stream()
                .map(employeeScheduleMapper::toResponse)
                .toList();
    }

    /**
     * Xử lý kiểm tra xung đột. Nếu force=true, xóa bản ghi cũ. Nếu force=false, thu thập vào danh sách conflicts.
     */
    private void processConflicts(Long employeeId, Shift newShift, Integer dayOfWeek, boolean force, List<ScheduleConflictDetail> conflicts) {
        // Lấy tất cả lịch đang hoạt động của nhân viên để kiểm tra
        List<EmployeeSchedule> existingSchedules = employeeScheduleRepository.findByEmployeeIdAndIsActiveTrue(employeeId);
        
        for (EmployeeSchedule existing : existingSchedules) {
            // Kiểm tra chồng lấn thời gian (xuyên ngày được xử lý trong ShiftUtils)
            if (ShiftUtils.isOverlapping(dayOfWeek, newShift, existing.getDayOfWeek(), existing.getShift())) {
                if (force) {
                    // Giữ lịch sử phân ca, chỉ ngưng hiệu lực bản ghi đang trùng.
                    existing.setIsActive(false);
                    employeeScheduleRepository.save(existing);
                } else {
                    // Nếu không bắt buộc, báo lỗi
                    conflicts.add(new ScheduleConflictDetail(
                            employeeId,
                            dayOfWeek,
                            newShift.getName(),
                            existing.getShift().getName()
                    ));
                }
            }
        }
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
    public PageResponse<EmployeeScheduleResponse> search(
            Long employeeId,
            Integer dayOfWeek,
            Boolean isActive,
            LocalDate effectiveFromOnOrBefore,
            Long shiftId,
            Pageable pageable) {
        Page<EmployeeSchedule> page = employeeScheduleRepository
                .findAll(
                        EmployeeScheduleSpecifications.matches(
                                employeeId, dayOfWeek, isActive, effectiveFromOnOrBefore, shiftId),
                        pageable);
        return PageResponse.of(page.map(employeeScheduleMapper::toResponse));
    }

    // Xóa lịch làm theo ID.
    @Transactional
    public void delete(Long id) {
        EmployeeSchedule schedule = employeeScheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        employeeScheduleRepository.delete(schedule);
    }
}



