package com.attendance.service;

import com.attendance.entity.Attendance;
import com.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    @Transactional
    public Attendance checkIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .map(attendance -> {
                    // Already checked in, return current or update if needed
                    return attendance;
                })
                .orElseGet(() -> {
                    Attendance attendance = Attendance.builder()
                            .employeeId(employeeId)
                            .workDate(today)
                            .checkInTime(LocalDateTime.now())
                            .status("PRESENT")
                            .build();
                    return attendanceRepository.save(attendance);
                });
    }

    @Transactional
    public Attendance checkOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));
        
        attendance.setCheckOutTime(LocalDateTime.now());
        // Logic for EARLY_LEAVE could be added here later
        return attendanceRepository.save(attendance);
    }
}
