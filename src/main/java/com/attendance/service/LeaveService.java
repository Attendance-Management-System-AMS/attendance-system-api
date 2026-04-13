package com.attendance.service;

import com.attendance.exception.AppException;
import com.attendance.dto.response.PageResponse;
import com.attendance.dto.request.LeaveRequestRecord;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveType;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.LeaveMapper;
import com.attendance.mapper.LeaveTypeMapper;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.LeaveRequestRepository;
import com.attendance.repository.spec.LeaveSpecifications;
import com.attendance.repository.LeaveTypeRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveMapper leaveMapper;
    private final LeaveTypeMapper leaveTypeMapper;

    // Khởi tạo service với repository và mapper cho đơn nghỉ.
    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository,
                        LeaveTypeRepository leaveTypeRepository,
                        LeaveMapper leaveMapper,
                        LeaveTypeMapper leaveTypeMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveMapper = leaveMapper;
        this.leaveTypeMapper = leaveTypeMapper;
    }

    // Tạo đơn xin nghỉ mới cho nhân viên.
    @Transactional
    public LeaveResponse createRequest(LeaveRequestRecord request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        LeaveType leaveType = leaveTypeRepository.findByCode(request.leaveTypeCode())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Loại nghỉ không hợp lệ"));

        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        boolean overlapped = leaveRequestRepository
                .existsByEmployeeIdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        employee.getId(),
                        List.of("PENDING", "APPROVED"),
                        request.toDate(),
                        request.fromDate());
        if (overlapped) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Khoảng ngày nghỉ bị trùng với đơn đang chờ duyệt hoặc đã duyệt");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setFromDate(request.fromDate());
        leaveRequest.setToDate(request.toDate());
        leaveRequest.setReason(request.reason());

        long days = ChronoUnit.DAYS.between(request.fromDate(), request.toDate()) + 1;
        leaveRequest.setTotalDays((double) days);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return leaveMapper.toResponse(saved);
    }

    // Tìm kiếm đơn nghỉ theo từ khoá, nhân viên và trạng thái.
    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> search(String keyword, Long employeeId, String status, Pageable pageable) {
        var spec = LeaveSpecifications.matches(keyword, employeeId, status);
        Page<LeaveRequest> page = leaveRequestRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(leaveMapper::toResponse));
    }

    // Lấy chi tiết đơn nghỉ theo ID.
    @Transactional(readOnly = true)
    public LeaveResponse getById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));
        return leaveMapper.toResponse(leaveRequest);
    }

    // Phê duyệt đơn nghỉ nếu vẫn đang ở trạng thái PENDING.
    @Transactional
    public LeaveResponse approve(Long id, Long approvedById) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể phê duyệt đơn đang chờ xử lý");
        }

        leaveRequest.setStatus("APPROVED");
        if (approvedById != null) {
            employeeRepository.findById(approvedById).ifPresent(leaveRequest::setApprovedBy);
        }
        return leaveMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    // Từ chối đơn nghỉ nếu vẫn đang ở trạng thái PENDING.
    @Transactional
    public LeaveResponse reject(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể từ chối đơn đang chờ xử lý");
        }

        leaveRequest.setStatus("REJECTED");
        return leaveMapper.toResponse(leaveRequestRepository.save(leaveRequest));
    }

    // Huỷ đơn nghỉ khi chưa được duyệt hoặc từ chối.
    @Transactional
    public void delete(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể huỷ đơn đang chờ xử lý");
        }

        leaveRequestRepository.delete(leaveRequest);
    }

    // Lấy tất cả loại nghỉ đang bật.
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findByIsActive(true)
                .stream()
                .map(leaveTypeMapper::toResponse)
                .toList();
    }
}




