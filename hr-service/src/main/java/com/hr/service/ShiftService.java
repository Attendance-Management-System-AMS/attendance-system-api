package com.hr.service;

import com.hr.dto.shift.ShiftRequest;
import com.hr.dto.shift.ShiftResponse;
import com.hr.entity.Shift;
import com.hr.exception.AppException;
import com.hr.exception.ErrorCode;
import com.hr.mapper.ShiftMapper;
import com.hr.repository.ShiftRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftMapper shiftMapper;

    public ShiftService(ShiftRepository shiftRepository, ShiftMapper shiftMapper) {
        this.shiftRepository = shiftRepository;
        this.shiftMapper = shiftMapper;
    }

    public ShiftResponse create(ShiftRequest request) {
        if (shiftRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Tên ca làm đã tồn tại");
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Giờ kết thúc phải sau giờ bắt đầu");
        }

        Shift shift = shiftMapper.toEntity(request);

        Shift saved = shiftRepository.save(shift);
        return shiftMapper.toResponse(saved);
    }

    public List<ShiftResponse> getAll() {
        return shiftRepository.findAll(Sort.by(Sort.Direction.ASC, "startTime"))
                .stream()
            .map(shiftMapper::toResponse)
                .toList();
    }
}
