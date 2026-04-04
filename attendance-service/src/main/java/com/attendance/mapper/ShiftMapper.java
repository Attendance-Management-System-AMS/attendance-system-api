package com.attendance.mapper;

import com.attendance.dto.shift.ShiftRequest;
import com.attendance.dto.shift.ShiftResponse;
import com.attendance.entity.Shift;
import org.springframework.stereotype.Component;

@Component
public class ShiftMapper {

    // Chuyển request tạo ca làm thành entity.
    public Shift toEntity(ShiftRequest request) {
        Shift shift = new Shift();
        shift.setName(request.name().trim());
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        shift.setBreakStart(request.breakStart());
        shift.setBreakEnd(request.breakEnd());
        shift.setGracePeriod(request.gracePeriod() == null ? 0 : request.gracePeriod());
        return shift;
    }

    // Chuyển entity ca làm sang response.
    public ShiftResponse toResponse(Shift shift) {
        return new ShiftResponse(
                shift.getId(),
                shift.getName(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getBreakStart(),
                shift.getBreakEnd(),
                shift.getGracePeriod(),
                shift.getCreatedAt()
        );
    }
}
