package com.attendance.mapper;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.entity.Attendance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    AttendanceResponse toResponse(Attendance attendance);
}
