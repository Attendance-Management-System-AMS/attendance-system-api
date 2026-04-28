package com.attendance.mapper;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    // Chuyển entity chấm công sang response.
    @Mapping(target = "employeeFullName", ignore = true)
    @Mapping(target = "employeeSnapshotCode", ignore = true)
    @Mapping(target = "employeeSnapshotDepartmentName", ignore = true)
    @Mapping(target = "employeeSnapshotPositionName", ignore = true)
    AttendanceResponse toResponse(Attendance attendance);
}



