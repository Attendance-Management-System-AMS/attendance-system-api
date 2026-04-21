package com.attendance.mapper;

import com.attendance.dto.response.AttendanceResponse;
import com.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {

    @Mapping(target = "employeeFullName", ignore = true)
    @Mapping(target = "employeeSnapshotCode", ignore = true)
    @Mapping(target = "employeeSnapshotDepartmentName", ignore = true)
    @Mapping(target = "employeeSnapshotPositionName", ignore = true)
    AttendanceResponse toResponse(Attendance attendance);
}
