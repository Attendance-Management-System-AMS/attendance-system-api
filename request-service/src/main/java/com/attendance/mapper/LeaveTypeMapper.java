package com.attendance.mapper;

import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.entity.LeaveType;
import org.springframework.stereotype.Component;

@Component
public class LeaveTypeMapper {

    public LeaveTypeResponse toResponse(LeaveType leaveType) {
        if (leaveType == null) {
            return null;
        }

        return new LeaveTypeResponse(
                leaveType.getId(),
                leaveType.getCode(),
                leaveType.getName(),
                leaveType.getIsPaid(),
                leaveType.getIsActive(),
                leaveType.getDescription());
    }

    public LeaveType toEntity(LeaveTypeResponse leaveTypeResponse) {
        if (leaveTypeResponse == null) {
            return null;
        }

        LeaveType leaveType = new LeaveType();
        leaveType.setId(leaveTypeResponse.id());
        leaveType.setCode(leaveTypeResponse.code());
        leaveType.setName(leaveTypeResponse.name());
        leaveType.setIsPaid(leaveTypeResponse.isPaid());
        leaveType.setIsActive(leaveTypeResponse.isActive());
        leaveType.setDescription(leaveTypeResponse.description());
        return leaveType;
    }
}
