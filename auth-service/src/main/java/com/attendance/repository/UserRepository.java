package com.attendance.repository;

import com.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Tìm user theo username.
    Optional<User> findByUsername(String username);

    // Tìm user theo username không phân biệt hoa thường.
    Optional<User> findFirstByUsernameIgnoreCase(String username);

    // Tìm user theo email.
    Optional<User> findByEmail(String email);

    // Tìm user theo email không phân biệt hoa thường.
    Optional<User> findFirstByEmailIgnoreCase(String email);

    // Kiểm tra username đã tồn tại chưa.
    Boolean existsByUsername(String username);

    // Kiểm tra username đã tồn tại chưa, không phân biệt hoa thường.
    Boolean existsByUsernameIgnoreCase(String username);

    // Kiểm tra email đã tồn tại chưa.
    Boolean existsByEmail(String email);

    // Kiểm tra email đã tồn tại chưa, không phân biệt hoa thường.
    Boolean existsByEmailIgnoreCase(String email);

    // Đếm số user đang mang một role cụ thể.
    long countByRoles_RoleName(String roleName);
}




