package com.attendance.repository;

import com.attendance.entity.OvertimeRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long>, JpaSpecificationExecutor<OvertimeRequest> {

    List<OvertimeRequest> findByEmployeeIdAndWorkDateAndStatusIn(
            Long employeeId,
            LocalDate workDate,
            Collection<String> statuses);

    List<OvertimeRequest> findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(LocalDate fromDate, LocalDate toDate);

    List<OvertimeRequest> findByEmployeeIdInAndWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
            List<Long> employeeIds,
            LocalDate fromDate,
            LocalDate toDate);
}
