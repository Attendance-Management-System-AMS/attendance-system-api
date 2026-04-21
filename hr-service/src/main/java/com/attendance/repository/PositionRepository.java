package com.attendance.repository;

import com.attendance.entity.Position;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PositionRepository extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {

    // Nạp trước phòng ban khi tìm danh sách chức vụ.
    @EntityGraph(attributePaths = {"department"})
    @Override
    Page<Position> findAll(Specification<Position> spec, Pageable pageable);

    // Tìm chức vụ theo tên.
    Optional<Position> findByName(String name);

    // Tìm chức vụ theo tên không phân biệt hoa thường và phòng ban.
    Optional<Position> findByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    // Kiểm tra chức vụ đã tồn tại trong phòng ban chưa.
    boolean existsByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    // Lấy toàn bộ chức vụ của một phòng ban.
    List<Position> findByDepartmentId(Long departmentId);
}




