package com.hr.mapper;

import com.hr.dto.leave.LeaveTypeResponse;
import com.hr.entity.LeaveType;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface LeaveTypeMapper {

    // Chuyển entity loại nghỉ sang response.
    LeaveTypeResponse toResponse(LeaveType leaveType);

    // Chuyển response loại nghỉ sang entity.
    LeaveType toEntity(LeaveTypeResponse leaveTypeResponse);
}
