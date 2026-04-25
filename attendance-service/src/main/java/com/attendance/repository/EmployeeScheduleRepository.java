package com.attendance.repository;

import com.attendance.entity.EmployeeSchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long>, JpaSpecificationExecutor<EmployeeSchedule> {

    boolean existsByShift_Id(Long shiftId);

    List<EmployeeSchedule> findByEmployeeId(Long employeeId);

    List<EmployeeSchedule> findByEmployeeIdAndIsActiveTrue(Long employeeId);

    List<EmployeeSchedule> findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqual(Long employeeId, LocalDate date);

    List<EmployeeSchedule> findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            Long employeeId,
            LocalDate date);

    List<EmployeeSchedule> findByDayOfWeekAndIsActiveTrue(Integer dayOfWeek);

    List<EmployeeSchedule> findByEffectiveFromLessThanEqual(LocalDate date);
}



