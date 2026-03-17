package com.attendance.service;

import com.common.exception.AppException;
import com.attendance.dto.holiday.HolidayRequest;
import com.attendance.dto.holiday.HolidayResponse;
import com.attendance.entity.Holiday;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.HolidayMapper;
import com.attendance.repository.HolidayRepository;
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

    @Transactional(readOnly = true)
    public HolidayResponse getById(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOLIDAY_NOT_FOUND));
        return holidayMapper.toResponse(holiday);
    }

    @Transactional
    public HolidayResponse update(Long id, HolidayRequest request) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOLIDAY_NOT_FOUND));

        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        holiday.setHolidayName(request.holidayName().trim());
        holiday.setFromDate(request.fromDate());
        holiday.setToDate(request.toDate());
        holiday.setIsPaid(request.isPaid());
        return holidayMapper.toResponse(holidayRepository.save(holiday));
    }

    @Transactional
    public void delete(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOLIDAY_NOT_FOUND));
        holidayRepository.delete(holiday);
    }
}
