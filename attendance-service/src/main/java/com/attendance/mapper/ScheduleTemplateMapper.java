package com.attendance.mapper;

import com.attendance.dto.request.ScheduleTemplateRequest;
import com.attendance.dto.response.ScheduleTemplateItemResponse;
import com.attendance.dto.response.ScheduleTemplateResponse;
import com.attendance.entity.ScheduleTemplate;
import com.attendance.entity.ScheduleTemplateItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = ShiftMapper.class)
public interface ScheduleTemplateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "name", expression = "java(request.name() == null ? null : request.name().trim())")
    ScheduleTemplate toEntity(ScheduleTemplateRequest request);

    ScheduleTemplateResponse toResponse(ScheduleTemplate template);

    ScheduleTemplateItemResponse toItemResponse(ScheduleTemplateItem item);
}
