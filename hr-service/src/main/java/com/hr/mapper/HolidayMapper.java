package com.hr.mapper;

import com.hr.dto.holiday.HolidayRequest;
import com.hr.dto.holiday.HolidayResponse;
import com.hr.entity.Holiday;
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
