package com.hr.repository;

import com.hr.entity.EmployeeSchedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {

    List<EmployeeSchedule> findByEmployeeId(Long employeeId);

    List<EmployeeSchedule> findByEmployeeIdAndIsActiveTrue(Long employeeId);

    List<EmployeeSchedule> findByDayOfWeekAndIsActiveTrue(Integer dayOfWeek);

    List<EmployeeSchedule> findByEffectiveFromLessThanEqual(LocalDate date);
}
