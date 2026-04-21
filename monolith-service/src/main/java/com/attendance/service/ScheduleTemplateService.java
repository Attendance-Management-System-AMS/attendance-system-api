package com.attendance.service;

import com.attendance.dto.request.ScheduleTemplateItemRequest;
import com.attendance.dto.request.ScheduleTemplateRequest;
import com.attendance.dto.response.ScheduleTemplateResponse;
import com.attendance.entity.ScheduleTemplate;
import com.attendance.entity.ScheduleTemplateItem;
import com.attendance.entity.Shift;
import com.attendance.exception.ErrorCode;
import com.attendance.exception.AppException;
import com.attendance.mapper.ScheduleTemplateMapper;
import com.attendance.repository.ScheduleTemplateRepository;
import com.attendance.repository.ShiftRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleTemplateService {

    private final ScheduleTemplateRepository templateRepository;
    private final ShiftRepository shiftRepository;
    private final ScheduleTemplateMapper templateMapper;

    @Transactional
    public ScheduleTemplateResponse create(ScheduleTemplateRequest request) {
        ScheduleTemplate template = templateMapper.toEntity(request);
        
        List<ScheduleTemplateItem> items = request.items().stream()
                .map(itemReq -> createItemEntity(template, itemReq))
                .collect(Collectors.toList());
        
        template.setItems(items);
        return templateMapper.toResponse(templateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public List<ScheduleTemplateResponse> getAll() {
        return templateRepository.findAll().stream()
                .map(templateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScheduleTemplateResponse getById(Long id) {
        ScheduleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND));
        return templateMapper.toResponse(template);
    }

    @Transactional
    public ScheduleTemplateResponse update(Long id, ScheduleTemplateRequest request) {
        ScheduleTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND));

        template.setName(request.name().trim());
        template.setDescription(request.description());
        
        // Cập nhật items: Xóa cũ, tạo mới (đơn giản nhất)
        template.getItems().clear();
        List<ScheduleTemplateItem> newItems = request.items().stream()
                .map(itemReq -> createItemEntity(template, itemReq))
                .collect(Collectors.toList());
        template.getItems().addAll(newItems);

        return templateMapper.toResponse(templateRepository.save(template));
    }

    @Transactional
    public void delete(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new AppException(ErrorCode.SCHEDULE_TEMPLATE_NOT_FOUND);
        }
        templateRepository.deleteById(id);
    }

    private ScheduleTemplateItem createItemEntity(ScheduleTemplate template, ScheduleTemplateItemRequest itemReq) {
        Shift shift = shiftRepository.findById(itemReq.shiftId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        
        ScheduleTemplateItem item = new ScheduleTemplateItem();
        item.setTemplate(template);
        item.setDayOfWeek(itemReq.dayOfWeek());
        item.setShift(shift);
        return item;
    }
}



