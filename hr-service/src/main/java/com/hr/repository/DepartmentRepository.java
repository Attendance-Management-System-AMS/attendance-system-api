package com.hr.repository;

import com.hr.entity.Department;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    // Tìm phòng ban theo tên.
    Optional<Department> findByName(String name);

    // Kiểm tra tên phòng ban đã tồn tại chưa.
    boolean existsByName(String name);
}
