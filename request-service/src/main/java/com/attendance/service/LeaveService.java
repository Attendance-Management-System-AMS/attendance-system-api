package com.attendance.service;

import com.attendance.client.AttendanceClient;
import com.attendance.client.HrClient;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.LeaveRequestRecord;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveType;
import com.attendance.exception.AppException;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.LeaveMapper;
import com.attendance.mapper.LeaveTypeMapper;
import com.attendance.repository.LeaveRequestRepository;
import com.attendance.repository.LeaveTypeRepository;
import com.attendance.repository.spec.LeaveSpecifications;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveMapper leaveMapper;
    private final LeaveTypeMapper leaveTypeMapper;
    private final HrClient hrClient;
    private final AttendanceClient attendanceClient;

    @Transactional
    public LeaveResponse createRequest(LeaveRequestRecord request) {
        if (request.employeeId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên là bắt buộc");
        }
        requireEmployee(request.employeeId());

        LeaveType leaveType = leaveTypeRepository.findByCode(request.leaveTypeCode().trim())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Loại nghỉ không hợp lệ"));

        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        boolean overlapped = leaveRequestRepository
                .existsByEmployeeIdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        request.employeeId(),
                        List.of("PENDING", "APPROVED"),
                        request.toDate(),
                        request.fromDate());
        if (overlapped) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Khoảng ngày nghỉ bị trùng với đơn đang chờ duyệt hoặc đã duyệt");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(request.employeeId());
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setFromDate(request.fromDate());
        leaveRequest.setToDate(request.toDate());
        leaveRequest.setReason(request.reason());

        long days = ChronoUnit.DAYS.between(request.fromDate(), request.toDate()) + 1;
        leaveRequest.setTotalDays((double) days);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> search(String keyword, Long employeeId, String status, Pageable pageable) {
        var spec = LeaveSpecifications.matches(keyword, employeeId, status);
        Page<LeaveRequest> page = leaveRequestRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public LeaveResponse getById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));
        return toResponse(leaveRequest);
    }

    @Transactional
    public LeaveResponse approve(Long id, Long approvedById) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể phê duyệt đơn đang chờ xử lý");
        }

        leaveRequest.setStatus("APPROVED");
        if (approvedById != null) {
            requireEmployee(approvedById);
            leaveRequest.setApprovedById(approvedById);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        syncApprovedLeaveAttendance(saved);
        return toResponse(saved);
    }

    @Transactional
    public LeaveResponse reject(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể từ chối đơn đang chờ xử lý");
        }

        leaveRequest.setStatus("REJECTED");
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public void delete(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể huỷ đơn đang chờ xử lý");
        }

        leaveRequestRepository.delete(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findByIsActive(true)
                .stream()
                .map(leaveTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasApprovedLeave(Long employeeId, LocalDate date) {
        return leaveRequestRepository.existsByEmployeeIdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                employeeId,
                "APPROVED",
                date,
                date);
    }

    private LeaveResponse toResponse(LeaveRequest leaveRequest) {
        HrEmployeeSnapshot employee = getEmployeeSnapshotOrNull(leaveRequest.getEmployeeId());
        HrEmployeeSnapshot approvedBy = leaveRequest.getApprovedById() == null
                ? null
                : getEmployeeSnapshotOrNull(leaveRequest.getApprovedById());
        return leaveMapper.toResponse(leaveRequest, employee, approvedBy);
    }

    private void requireEmployee(Long employeeId) {
        if (!hrClient.employeeExists(employeeId)) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
    }

    private HrEmployeeSnapshot getEmployeeSnapshotOrNull(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        try {
            return hrClient.getEmployeeSnapshot(employeeId);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void syncApprovedLeaveAttendance(LeaveRequest leaveRequest) {
        try {
            attendanceClient.syncApprovedLeave(
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getToDate());
        } catch (Exception ex) {
            throw new AppException(
                    ErrorCode.UNCATEGORIZED_ERROR,
                    "Không thể đồng bộ bảng công sau khi duyệt đơn nghỉ");
        }
    }
}
