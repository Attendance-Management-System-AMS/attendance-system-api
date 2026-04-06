package com.attendance.mapper;

import com.attendance.dto.template.ScheduleTemplateItemResponse;
import com.attendance.dto.template.ScheduleTemplateRequest;
import com.attendance.dto.template.ScheduleTemplateResponse;
import com.attendance.entity.ScheduleTemplate;
import com.attendance.entity.ScheduleTemplateItem;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleTemplateMapper {

    private final ShiftMapper shiftMapper;

    public ScheduleTemplate toEntity(ScheduleTemplateRequest request) {
        ScheduleTemplate template = new ScheduleTemplate();
        template.setName(request.name().trim());
        template.setDescription(request.description());
        // Items are usually handled in service to link shifts
        return template;
    }

    public ScheduleTemplateResponse toResponse(ScheduleTemplate template) {
        return new ScheduleTemplateResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getItems().stream()
                        .map(this::toItemResponse)
                        .collect(Collectors.toList())
        );
    }

    public ScheduleTemplateItemResponse toItemResponse(ScheduleTemplateItem item) {
        return new ScheduleTemplateItemResponse(
                item.getId(),
                item.getDayOfWeek(),
                shiftMapper.toResponse(item.getShift())
        );
    }
}
