package com.hr.service;

import com.hr.dto.holiday.HolidayRequest;
import com.hr.dto.holiday.HolidayResponse;
import com.hr.entity.Holiday;
import com.hr.exception.AppException;
import com.hr.exception.ErrorCode;
import com.hr.mapper.HolidayMapper;
import com.hr.repository.HolidayRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    public HolidayService(HolidayRepository holidayRepository, HolidayMapper holidayMapper) {
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
    }

    @Transactional
    public HolidayResponse create(HolidayRequest request) {
        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        Holiday holiday = holidayMapper.toEntity(request);
        Holiday saved = holidayRepository.save(holiday);
        return holidayMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HolidayResponse> getAll() {
        return holidayRepository.findAll(Sort.by(Sort.Direction.ASC, "fromDate"))
                .stream()
                .map(holidayMapper::toResponse)
                .toList();
    }
}
