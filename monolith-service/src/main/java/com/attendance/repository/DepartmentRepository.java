package com.attendance.repository;

import com.attendance.dto.response.DepartmentResponse;
import com.attendance.entity.Department;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

       // Tìm phòng ban theo tên.
       Optional<Department> findByName(String name);

       // Kiểm tra tên phòng ban đã tồn tại chưa.
       boolean existsByName(String name);

       // Truy vấn danh sách phòng ban kèm số lượng nhân viên bằng constructor
       // expression.
       @Query("SELECT new com.attendance.dto.response.DepartmentResponse(d.id, d.name, d.description, d.status, COUNT(e), d.createdAt) "
                     +
                     "FROM Department d " +
                     "LEFT JOIN Employee e ON e.department.id = d.id " +
                     "WHERE (CAST(:keyword AS string) IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
                     "GROUP BY d.id, d.name, d.description, d.status, d.createdAt")
       Page<DepartmentResponse> findAllWithCount(@Param("keyword") String keyword, Pageable pageable);

       // Truy vấn chi tiết phòng ban kèm số lượng nhân viên.
       @Query("SELECT new com.attendance.dto.response.DepartmentResponse(d.id, d.name, d.description, d.status, COUNT(e), d.createdAt) "
                     +
                     "FROM Department d " +
                     "LEFT JOIN Employee e ON e.department.id = d.id " +
                     "WHERE d.id = :id " +
                     "GROUP BY d.id, d.name, d.description, d.status, d.createdAt")
       Optional<DepartmentResponse> findByIdWithCount(@Param("id") Long id);
}

