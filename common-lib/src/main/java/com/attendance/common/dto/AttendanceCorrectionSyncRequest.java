package com.attendance.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceCorrectionSyncRequest(
        Long employeeId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate workDate,
        @JsonFormat(pattern = "HH:mm") LocalTime correctedCheckIn,
        @JsonFormat(pattern = "HH:mm") LocalTime correctedCheckOut) {}
