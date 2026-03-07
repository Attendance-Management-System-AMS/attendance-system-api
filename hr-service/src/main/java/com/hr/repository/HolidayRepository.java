package com.hr.repository;

import com.hr.entity.Holiday;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByFromDateLessThanEqualAndToDateGreaterThanEqual(LocalDate fromDate, LocalDate toDate);

    List<Holiday> findByIsPaidTrue();
}
