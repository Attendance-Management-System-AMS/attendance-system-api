package com.hr.repository;

import com.hr.entity.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    List<Employee> findByStatusAndFaceEmbeddingIsNotNull(String status);

    @EntityGraph(attributePaths = {"department", "position", "manager"})
    @Override
    List<Employee> findAll(Specification<Employee> spec, Sort sort);

    @EntityGraph(attributePaths = {"department", "position", "manager"})
    @Override
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"department", "position", "manager"})
    Optional<Employee> findById(Long id);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByDepartmentId(Long departmentId);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);
}
