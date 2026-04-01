package com.hr.mapper;

import com.hr.dto.leave.LeaveTypeResponse;
import com.hr.entity.LeaveType;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface LeaveTypeMapper {

    LeaveTypeResponse toResponse(LeaveType leaveType);

    LeaveType toEntity(LeaveTypeResponse leaveTypeResponse);
}
