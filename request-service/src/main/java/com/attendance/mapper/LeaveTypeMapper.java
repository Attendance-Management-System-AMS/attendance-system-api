package com.attendance.mapper;

import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.entity.LeaveType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeaveTypeMapper {

    LeaveTypeResponse toResponse(LeaveType leaveType);

    LeaveType toEntity(LeaveTypeResponse leaveTypeResponse);
}
