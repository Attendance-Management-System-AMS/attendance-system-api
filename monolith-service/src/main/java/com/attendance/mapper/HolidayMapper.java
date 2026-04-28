package com.attendance.mapper;

import com.attendance.dto.request.HolidayRequest;
import com.attendance.dto.response.HolidayResponse;
import com.attendance.entity.Holiday;
import org.springframework.stereotype.Component;

@Component
public class HolidayMapper {

    // Chuyển request ngày nghỉ thành entity.
    public Holiday toEntity(HolidayRequest request) {
        Holiday holiday = new Holiday();
        holiday.setHolidayName(request.holidayName().trim());
        holiday.setFromDate(request.fromDate());
        holiday.setToDate(request.toDate());
        holiday.setIsPaid(request.isPaid());
        return holiday;
    }

    // Chuyển entity ngày nghỉ sang response.
    public HolidayResponse toResponse(Holiday holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getHolidayName(),
                holiday.getFromDate(),
                holiday.getToDate(),
                holiday.getIsPaid()
        );
    }
}



