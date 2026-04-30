package com.attendance.mapper;

import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.entity.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveResponse toResponse(
            LeaveRequest leaveRequest,
            HrEmployeeSnapshot employee,
            HrEmployeeSnapshot approvedBy) {
        return new LeaveResponse(
                leaveRequest.getId(),
                leaveRequest.getEmployeeId(),
                employee == null ? null : employee.fullName(),
                employee == null ? null : employee.employeeCode(),
                employee == null ? null : employee.departmentName(),
                employee == null ? null : employee.positionName(),
                leaveRequest.getLeaveType() == null ? null : leaveRequest.getLeaveType().getCode(),
                leaveRequest.getLeaveType() == null ? null : leaveRequest.getLeaveType().getName(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getTotalDays(),
                leaveRequest.getReason(),
                leaveRequest.getStatus(),
                approvedBy == null ? null : approvedBy.fullName(),
                leaveRequest.getCreatedAt(),
                leaveRequest.getCorrectedCheckIn(),
                leaveRequest.getCorrectedCheckOut());
    }
}
