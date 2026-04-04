package com.attendance.service;

import com.common.exception.AppException;
import com.attendance.dto.holiday.HolidayRequest;
import com.attendance.dto.holiday.HolidayResponse;
import com.attendance.entity.Holiday;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.HolidayMapper;
import com.attendance.repository.HolidayRepository;
import com.attendance.repository.HolidaySpecifications;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;

    // Khởi tạo service xử lý ngày nghỉ.
    public HolidayService(HolidayRepository holidayRepository, HolidayMapper holidayMapper) {
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
    }

    // Tạo mới ngày nghỉ và kiểm tra ngày hợp lệ.
    @Transactional
    public HolidayResponse create(HolidayRequest request) {
        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }

        Holiday holiday = holidayMapper.toEntity(request);
        Holiday saved = holidayRepository.save(holiday);
        return holidayMapper.toResponse(saved);
    }

    // Lấy toàn bộ danh sách ngày nghỉ.
    @Transactional(readOnly = true)
    public List<HolidayResponse> getAll() {
        return holidayRepository.findAll(Sort.by(Sort.Direction.ASC, "fromDate"))
                .stream()
                .map(holidayMapper::toResponse)
                .toList();
    }

    // Tìm kiếm ngày nghỉ theo bộ lọc và phân trang.
    @Transactional(readOnly = true)
    public Page<HolidayResponse> search(
            String keyword,
            Boolean isPaid,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {
        return holidayRepository.findAll(HolidaySpecifications.matches(keyword, isPaid, fromDate, toDate), pageable)
                .map(holidayMapper::toResponse);
    }

    // Lấy ngày nghỉ theo ID.
    @Transactional(readOnly = true)
    public HolidayResponse getById(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOLIDAY_NOT_FOUND));
        return holidayMapper.toResponse(holiday);
    }

    // Cập nhật thông tin ngày nghỉ.
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

    // Xóa ngày nghỉ theo ID.
    @Transactional
    public void delete(Long id) {
        Holiday holiday = holidayRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOLIDAY_NOT_FOUND));
        holidayRepository.delete(holiday);
    }
}
