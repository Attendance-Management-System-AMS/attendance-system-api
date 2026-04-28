package com.attendance.mapper;

import com.attendance.dto.request.HolidayRequest;
import com.attendance.dto.response.HolidayResponse;
import com.attendance.entity.Holiday;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface HolidayMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "holidayName", source = "holidayName", qualifiedByName = "trim")
    Holiday toEntity(HolidayRequest request);

    HolidayResponse toResponse(Holiday holiday);

    @Named("trim")
    default String trim(String value) {
        return value == null ? null : value.trim();
    }
}
