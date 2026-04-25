package com.attendance.repository;

import com.attendance.entity.ScheduleTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleTemplateItemRepository extends JpaRepository<ScheduleTemplateItem, Long> {

    boolean existsByShift_Id(Long shiftId);
}
