package com.hr.mapper;

import com.hr.dto.leave.LeaveResponse;
import com.hr.entity.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveResponse toResponse(LeaveRequest request) {
        return new LeaveResponse(
                request.getId(),
                request.getEmployee().getId(),
                request.getEmployee().getFullName(),
                request.getLeaveType(),
                request.getFromDate(),
                request.getToDate(),
                request.getTotalDays(),
                request.getReason(),
                request.getStatus(),
                request.getApprovedBy() != null ? request.getApprovedBy().getId() : null,
                request.getCreatedAt()
        );
    }
}
