package com.attendance.mapper;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "employee", ignore = true)
    AttendanceResponse toResponse(Attendance attendance);
}
