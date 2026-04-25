package com.attendance.mapper;

import com.attendance.dto.response.EmployeeScheduleResponse;
import com.attendance.entity.EmployeeSchedule;
import java.time.LocalTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface EmployeeScheduleMapper {

    @Mapping(target = "employeeName", ignore = true)
    @Mapping(target = "shiftId", source = "shift.id")
    @Mapping(target = "shiftName", source = "shift.name")
    @Mapping(target = "startTime", source = "shift.startTime", qualifiedByName = "localTimeToString")
    @Mapping(target = "endTime", source = "shift.endTime", qualifiedByName = "localTimeToString")
    EmployeeScheduleResponse toResponse(EmployeeSchedule schedule);

    @Named("localTimeToString")
    default String localTimeToString(LocalTime value) {
        return value == null ? null : value.toString();
    }
}
