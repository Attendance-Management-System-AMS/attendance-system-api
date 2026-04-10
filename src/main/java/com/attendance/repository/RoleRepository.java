package com.attendance.repository;

import com.attendance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Tìm role theo tên role.
    Optional<Role> findByRoleName(String roleName);

    // Lấy danh sách role theo tập tên truyền vào.
    List<Role> findByRoleNameIn(Iterable<String> roleNames);

    // Lấy toàn bộ role và sắp xếp theo tên tăng dần.
    List<Role> findAllByOrderByRoleNameAsc();
}




