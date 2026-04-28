package com.attendance.repository;

import com.attendance.entity.Employee;
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

    // Lấy danh sách nhân viên đã có face embedding.
    List<Employee> findByStatusAndFaceEmbeddingIsNotNull(String status);

    // Nạp trước các quan hệ khi tìm danh sách nhân viên.
    @EntityGraph(attributePaths = {"department", "position", "manager"})
    @Override
    List<Employee> findAll(Specification<Employee> spec, Sort sort);

    // Nạp trước các quan hệ khi tìm danh sách nhân viên có phân trang.
    @EntityGraph(attributePaths = {"department", "position", "manager"})
    @Override
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);

    // Nạp trước các quan hệ khi tìm nhân viên theo ID.
    @Override
    @EntityGraph(attributePaths = {"department", "position", "manager"})
    Optional<Employee> findById(Long id);

    // Tìm nhân viên theo mã nhân viên.
    Optional<Employee> findByEmployeeCode(String employeeCode);

    // Tìm nhân viên theo email.
    Optional<Employee> findByEmail(String email);

    // Lấy danh sách nhân viên theo phòng ban.
    List<Employee> findByDepartmentId(Long departmentId);

    // Kiểm tra mã nhân viên đã tồn tại chưa.
    boolean existsByEmployeeCode(String employeeCode);

    // Kiểm tra email đã tồn tại chưa.
    boolean existsByEmail(String email);

    // Tìm nhân viên theo userId.
    @EntityGraph(attributePaths = {"department", "position"})
    Optional<Employee> findByUserId(Long userId);
}




