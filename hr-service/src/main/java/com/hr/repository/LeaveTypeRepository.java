package com.hr.repository;

import com.hr.entity.LeaveType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    Optional<LeaveType> findByCode(String code);

    Optional<LeaveType> findByName(String name);

    List<LeaveType> findByIsActive(Boolean isActive);
}
