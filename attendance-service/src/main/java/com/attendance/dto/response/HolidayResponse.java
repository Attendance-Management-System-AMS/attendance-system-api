package com.attendance.dto.response;

import java.time.LocalDate;

public record HolidayResponse(
        Long id,
        String holidayName,
        LocalDate fromDate,
        LocalDate toDate,
        Boolean isPaid
) {
}


