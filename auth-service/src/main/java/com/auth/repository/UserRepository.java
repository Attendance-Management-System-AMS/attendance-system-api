package com.auth.repository;

import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm user theo username.
    Optional<User> findByUsername(String username);

    // Tìm user theo email.
    Optional<User> findByEmail(String email);

    // Kiểm tra username đã tồn tại chưa.
    Boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa.
    Boolean existsByEmail(String email);
}
