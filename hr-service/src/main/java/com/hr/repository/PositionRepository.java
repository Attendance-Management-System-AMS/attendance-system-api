package com.hr.repository;

import com.hr.entity.Position;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {

    @EntityGraph(attributePaths = {"department"})
    @Override
    Page<Position> findAll(Specification<Position> spec, Pageable pageable);

    Optional<Position> findByName(String name);

    Optional<Position> findByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    boolean existsByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    List<Position> findByDepartmentId(Long departmentId);
}
