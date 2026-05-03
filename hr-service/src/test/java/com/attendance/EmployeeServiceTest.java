package com.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.client.AuthClient;
import com.attendance.dto.response.EmployeeInternalResponse;
import com.attendance.dto.request.UpdateEmployeeRequest;
import com.attendance.entity.Employee;
import com.attendance.mapper.EmployeeMapper;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.PositionRepository;
import com.attendance.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final double FACE_MATCH_THRESHOLD = 0.55d;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AuthClient authClient;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(
                employeeRepository,
                departmentRepository,
                positionRepository,
                Mappers.getMapper(EmployeeMapper.class),
                new ObjectMapper(),
                authClient,
                FACE_MATCH_THRESHOLD);
    }

    @Test
    void updateSyncsEmployeeIdentityToAuthAccount() {
        Employee employee = createEmployee(7L, 13L, "EMP-0007", "old@company.com", "ACTIVE");
        UpdateEmployeeRequest request = new UpdateEmployeeRequest(
                "EMP-7001",
                "Nguyen Van B",
                "MALE",
                "new@company.com",
                null,
                null,
                null,
                "INACTIVE",
                null,
                LocalDate.of(2026, 4, 29));

        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmployeeCode("EMP-7001")).thenReturn(false);
        when(employeeRepository.findByEmail("new@company.com")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = employeeService.update(7L, request);

        assertThat(response.employeeCode()).isEqualTo("EMP-7001");
        assertThat(response.email()).isEqualTo("new@company.com");
        assertThat(response.status()).isEqualTo("INACTIVE");
        verify(authClient).updateUser(eq(13L), argThat(syncRequest ->
                "EMP-7001".equals(syncRequest.username())
                        && "new@company.com".equals(syncRequest.email())
                        && !syncRequest.enabled()));
    }

    @Test
    void deleteDisablesLinkedAuthAccount() {
        Employee employee = createEmployee(7L, 13L, "EMP-0007", "old@company.com", "ACTIVE");

        when(employeeRepository.findById(7L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        employeeService.delete(7L);

        assertThat(employee.getStatus()).isEqualTo("INACTIVE");
        verify(authClient).updateUser(eq(13L), argThat(syncRequest ->
                "EMP-0007".equals(syncRequest.username())
                        && "old@company.com".equals(syncRequest.email())
                        && !syncRequest.enabled()));
    }

    @Test
    void getInternalEmployeesLoadsProfilesInBatch() {
        Employee admin = createEmployee(1L, 101L, "EMP-0101", "admin@company.com", "ACTIVE");
        admin.setFullName("System Admin");
        Employee manager = createEmployee(2L, 102L, "EMP-0102", "manager@company.com", "ACTIVE");
        manager.setFullName("Nguyen Van Manager");

        when(employeeRepository.findByUserIdIn(List.of(101L, 102L))).thenReturn(List.of(admin, manager));

        List<EmployeeInternalResponse> profiles = employeeService.getInternalEmployees(List.of(101L, 102L, 101L));

        assertThat(profiles).hasSize(2);
        assertThat(profiles).extracting(EmployeeInternalResponse::getUserId).containsExactly(101L, 102L);
        assertThat(profiles).extracting(EmployeeInternalResponse::getFullName)
                .containsExactly("System Admin", "Nguyen Van Manager");
        verify(employeeRepository).findByUserIdIn(List.of(101L, 102L));
    }

    private Employee createEmployee(Long id, Long userId, String employeeCode, String email, String status) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUserId(userId);
        employee.setEmployeeCode(employeeCode);
        employee.setFullName("Nguyen Van A");
        employee.setEmail(email);
        employee.setStatus(status);
        return employee;
    }
}
