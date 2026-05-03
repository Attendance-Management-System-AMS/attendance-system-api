package com.attendance.repository;

import com.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    Optional<Attendance> findFirstByEmployeeIdAndCheckOutTimeIsNullAndWorkDateBetweenOrderByWorkDateDesc(
            Long employeeId,
            LocalDate fromDate,
            LocalDate workDate);

    List<Attendance> findByEmployeeIdOrderByWorkDateDesc(Long employeeId);

    List<Attendance> findByEmployeeIdInAndWorkDateBetween(List<Long> employeeIds, LocalDate fromDate, LocalDate toDate);

    List<Attendance> findByEmployeeIdInAndWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
            List<Long> employeeIds,
            LocalDate fromDate,
            LocalDate toDate);

    List<Attendance> findByWorkDateOrderByEmployeeIdAsc(LocalDate workDate);

    List<Attendance> findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(LocalDate fromDate, LocalDate toDate);
}



