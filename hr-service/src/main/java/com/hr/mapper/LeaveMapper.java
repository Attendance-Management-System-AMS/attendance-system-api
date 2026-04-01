package com.hr.mapper;

import com.hr.dto.leave.LeaveResponse;
import com.hr.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = {LeaveTypeMapper.class})
public interface LeaveMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "leaveType", target = "leaveType")
    @Mapping(source = "approvedBy.id", target = "approvedBy")
    LeaveResponse toResponse(LeaveRequest leaveRequest);
}
