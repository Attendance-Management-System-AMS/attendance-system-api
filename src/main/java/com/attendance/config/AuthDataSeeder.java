package com.attendance.config;

import com.attendance.entity.Role;
import com.attendance.entity.User;
import com.attendance.repository.RoleRepository;
import com.attendance.repository.UserRepository;
import com.attendance.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AuthDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Tạo sẵn dữ liệu mẫu khi ứng dụng khởi động.
    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole(AuthService.ROLE_ADMIN, "Quản trị và kiểm toán hệ thống");
        Role hrRole = getOrCreateRole(AuthService.ROLE_HR, "Nhân sự và vận hành chấm công");
        Role managerRole = getOrCreateRole(AuthService.ROLE_MANAGER, "Quản lý phòng ban");
        Role employeeRole = getOrCreateRole(AuthService.ROLE_EMPLOYEE, "Nhân viên thông thường");

        createUserIfMissing("admin", "Admin@123", "admin@company.com", Set.of(adminRole));
        createUserIfMissing("hr", "Hr@12345", "hr@company.com", Set.of(hrRole));
        createUserIfMissing("manager", "Manager@123", "manager@company.com", Set.of(managerRole));
        createUserIfMissing("employee", "Employee@123", "employee@company.com", Set.of(employeeRole));
    }

    // Lấy role nếu đã có, chưa có thì tạo mới.
    private Role getOrCreateRole(String roleName, String description) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(roleName)
                        .description(description)
                        .build()));
    }

    // Tạo tài khoản nếu username chưa tồn tại.
    private void createUserIfMissing(String username, String rawPassword, String email, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .isEnabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info("Đã seed tài khoản mặc định auth: {}", username);
    }
}




