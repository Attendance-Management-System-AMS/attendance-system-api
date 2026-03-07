package com.hr.repository;

import com.hr.entity.Position;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByName(String name);

    List<Position> findByDepartmentId(Long departmentId);
}
