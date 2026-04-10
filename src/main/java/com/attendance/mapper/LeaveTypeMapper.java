package com.attendance.mapper;

import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.entity.LeaveType;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface LeaveTypeMapper {

    // Chuyển entity loại nghỉ sang response.
    LeaveTypeResponse toResponse(LeaveType leaveType);

    // Chuyển response loại nghỉ sang entity.
    LeaveType toEntity(LeaveTypeResponse leaveTypeResponse);
}




