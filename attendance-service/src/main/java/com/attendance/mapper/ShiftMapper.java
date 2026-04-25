package com.attendance.mapper;

import com.attendance.dto.request.ShiftRequest;
import com.attendance.dto.response.ShiftResponse;
import com.attendance.entity.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface ShiftMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "trim")
    @Mapping(target = "gracePeriod", source = "gracePeriod", qualifiedByName = "defaultGracePeriod")
    Shift toEntity(ShiftRequest request);

    ShiftResponse toResponse(Shift shift);

    @Named("trim")
    default String trim(String value) {
        return value == null ? null : value.trim();
    }

    @Named("defaultGracePeriod")
    default Integer defaultGracePeriod(Integer value) {
        return value == null ? 0 : value;
    }
}
