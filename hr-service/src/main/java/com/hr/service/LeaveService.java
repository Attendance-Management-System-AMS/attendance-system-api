package com.hr.service;

import com.common.exception.AppException;
import com.hr.dto.leave.LeaveRequestRecord;
import com.hr.dto.leave.LeaveResponse;
import com.hr.entity.Employee;
import com.hr.entity.LeaveRequest;
import com.hr.exception.ErrorCode;
import com.hr.mapper.LeaveMapper;
import com.hr.repository.EmployeeRepository;
import com.hr.repository.LeaveRequestRepository;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        EmployeeRepository employeeRepository,
                        LeaveMapper leaveMapper) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveMapper = leaveMapper;
    }

    @Transactional
    public LeaveResponse createRequest(LeaveRequestRecord request) {
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(request.leaveType());
        leaveRequest.setFromDate(request.fromDate());
        leaveRequest.setToDate(request.toDate());
        leaveRequest.setReason(request.reason());
        
        // Simple calculation: include both start and end date
        long days = ChronoUnit.DAYS.between(request.fromDate(), request.toDate()) + 1;
        leaveRequest.setTotalDays((double) days);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return leaveMapper.toResponse(saved);
    }

    public List<LeaveResponse> getByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId)
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }
}
