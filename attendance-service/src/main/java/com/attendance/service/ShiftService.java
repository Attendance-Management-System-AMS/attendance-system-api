package com.attendance.service;

import com.common.pagination.PageResponse;

import com.common.exception.AppException;
import com.attendance.dto.shift.ShiftRequest;
import com.attendance.dto.shift.ShiftResponse;
import com.attendance.entity.Shift;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.ShiftMapper;
import com.attendance.repository.ShiftRepository;
import com.attendance.repository.ShiftSpecifications;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    // Khởi tạo service xử lý ca làm.
    public ShiftService(ShiftRepository shiftRepository, ShiftMapper shiftMapper) {
        this.shiftRepository = shiftRepository;
        this.shiftMapper = shiftMapper;
    }

    // Tạo mới ca làm và kiểm tra trùng tên.
    public ShiftResponse create(ShiftRequest request) {
        if (shiftRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Tên ca làm đã tồn tại");
        }

        // Kiểm tra: endTime sau startTime HOẶC endTime trước startTime (ca qua nửa đêm)
        if (!request.endTime().isAfter(request.startTime()) && 
            !request.endTime().isBefore(request.startTime())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Giờ kết thúc phải sau giờ bắt đầu");
        }

        Shift shift = shiftMapper.toEntity(request);

        Shift saved = shiftRepository.save(shift);
        return shiftMapper.toResponse(saved);
    }

    // Lấy toàn bộ ca làm theo giờ bắt đầu.
    public List<ShiftResponse> getAll() {
        return shiftRepository.findAll(Sort.by(Sort.Direction.ASC, "startTime"))
                .stream()
                .map(shiftMapper::toResponse)
                .toList();
    }

    // Tìm kiếm ca làm theo từ khoá.
    public PageResponse<ShiftResponse> search(String keyword, Pageable pageable) {
        Page<Shift> page = shiftRepository.findAll(ShiftSpecifications.matches(keyword), pageable);
        return PageResponse.of(page.map(shiftMapper::toResponse));
    }

    // Lấy ca làm theo ID.
    public ShiftResponse getById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        return shiftMapper.toResponse(shift);
    }

    // Cập nhật thông tin ca làm và kiểm tra hợp lệ.
    public ShiftResponse update(Long id, ShiftRequest request) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        shiftRepository.findByName(request.name().trim())
                .filter(found -> !found.getId().equals(shift.getId()))
                .ifPresent(found -> {
                    throw new AppException(ErrorCode.INVALID_INPUT, "Tên ca làm đã tồn tại");
                });

        // Kiểm tra: endTime sau startTime HOẶC endTime trước startTime (ca qua nửa đêm)
        if (!request.endTime().isAfter(request.startTime()) && 
            !request.endTime().isBefore(request.startTime())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Giờ kết thúc phải sau giờ bắt đầu");
        }

        shift.setName(request.name().trim());
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setBreakStart(request.breakStart());
        shift.setBreakEnd(request.breakEnd());
        shift.setGracePeriod(request.gracePeriod() == null ? 0 : request.gracePeriod());
        return shiftMapper.toResponse(shiftRepository.save(shift));
    }

    // Xóa ca làm theo ID.
    public void delete(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        shiftRepository.delete(shift);
    }
}
