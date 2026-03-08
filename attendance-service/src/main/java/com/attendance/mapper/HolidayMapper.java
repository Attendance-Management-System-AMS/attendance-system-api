package com.attendance.mapper;

import com.attendance.dto.holiday.HolidayRequest;
import com.attendance.dto.holiday.HolidayResponse;
import com.attendance.entity.Holiday;
import org.springframework.stereotype.Component;

@Component
public class HolidayMapper {

    public Holiday toEntity(HolidayRequest request) {
        Holiday holiday = new Holiday();
        holiday.setHolidayName(request.holidayName().trim());
        holiday.setFromDate(request.fromDate());
        holiday.setToDate(request.toDate());
        holiday.setIsPaid(request.isPaid());
        return holiday;
    }

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
