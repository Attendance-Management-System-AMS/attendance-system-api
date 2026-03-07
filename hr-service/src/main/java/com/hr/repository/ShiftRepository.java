package com.hr.repository;

import com.hr.entity.Shift;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    Optional<Shift> findByName(String name);

    boolean existsByName(String name);
}
