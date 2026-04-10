package com.attendance.config;

import com.attendance.entity.*;
import com.attendance.repository.*;
import com.attendance.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@Order(2) // Run after AuthDataSeeder (Order 1 is default for others, or I can set Order(1) for AuthDataSeeder)
@RequiredArgsConstructor
public class DummyDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Employee@123";

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 4) {
            log.info("Dữ liệu nhân viên đã tồn tại, bỏ qua bước seed dummy data.");
            return;
        }

        log.info("Bắt đầu seed 50 nhân viên giả lập...");

        Role employeeRole = roleRepository.findByRoleName(AuthService.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("ROLE_EMPLOYEE not found"));

        List<Department> departments = departmentRepository.findAll();
        List<Position> positions = positionRepository.findAll();

        if (departments.isEmpty() || positions.isEmpty()) {
            log.warn("Không tìm thấy phòng ban hoặc chức vụ. Vui lòng kiểm tra V2 migration.");
            return;
        }

        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô"};
        String[] middleNames = {"Văn", "Thị", "Anh", "Minh", "Hữu", "Đức", "Thành", "Quang", "Hồng", "Tuấn"};
        String[] lastNames = {"Hùng", "Dũng", "Cường", "Trang", "Linh", "Hương", "Tuấn", "Khang", "Huy", "Nam", "Yến", "Mai", "Phúc", "Tâm", "Thảo"};

        Random random = new Random();
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        for (int i = 1; i <= 50; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String middleName = middleNames[random.nextInt(middleNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            String fullName = firstName + " " + middleName + " " + lastName;
            
            String username = "user" + i;
            String employeeCode = String.format("EMP%03d", i);
            String email = username + "@company.com";

            // 1. Create User
            User user = User.builder()
                    .username(username)
                    .password(encodedPassword)
                    .email(email)
                    .isEnabled(true)
                    .roles(Set.of(employeeRole))
                    .build();
            user = userRepository.save(user);

            // 2. Assign Random Department and matching Position
            Department dept = departments.get(random.nextInt(departments.size()));
            List<Position> deptPositions = positions.stream()
                    .filter(p -> p.getDepartment().getId().equals(dept.getId()))
                    .toList();
            Position pos = deptPositions.get(random.nextInt(deptPositions.size()));

            // 3. Create Employee
            Employee employee = Employee.builder()
                    .userId(user.getId())
                    .employeeCode(employeeCode)
                    .fullName(fullName)
                    .gender(random.nextBoolean() ? "Nam" : "Nữ")
                    .email(email)
                    .department(dept)
                    .position(pos)
                    .status("ACTIVE")
                    .joinDate(LocalDate.now().minusMonths(random.nextInt(24)))
                    .build();
            
            employeeRepository.save(employee);
        }

        log.info("Hoàn tất seed 50 nhân viên thành công.");
    }
}
