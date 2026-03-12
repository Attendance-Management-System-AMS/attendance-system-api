package com.hr.repository;

import com.hr.entity.Position;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByName(String name);

    Optional<Position> findByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    boolean existsByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    List<Position> findByDepartmentId(Long departmentId);
}
