package com.attendance.mapper;

import com.attendance.dto.response.LeaveResponse;
import com.attendance.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = {LeaveTypeMapper.class})
public interface LeaveMapper {

    // Chuyển entity đơn nghỉ sang response.
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeName")
    @Mapping(source = "leaveType.code", target = "leaveTypeCode")
    @Mapping(source = "leaveType.name", target = "leaveTypeName")
    @Mapping(source = "approvedBy.fullName", target = "approvedByName")
    LeaveResponse toResponse(LeaveRequest leaveRequest);
}




