package com.attendance.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.common.dto.PageResponse;
import com.attendance.common.error.GlobalExceptionHandler;
import com.attendance.controller.AdminAuthController;
import com.attendance.dto.request.AdminUpdateUserAccessRequest;
import com.attendance.dto.response.AdminUserResponse;
import com.attendance.dto.response.RoleSummaryResponse;
import com.attendance.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminAuthController adminAuthController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(adminAuthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getRolesReturnsSummaries() throws Exception {
        when(authService.getRoleSummaries()).thenReturn(List.of(
                new RoleSummaryResponse("ROLE_ADMIN", "Quan tri", 1),
                new RoleSummaryResponse("ROLE_HR", "Nhan su", 2)));

        mockMvc.perform(get("/api/auth/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].roleName").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.result[1].userCount").value(2));
    }

    @Test
    void getAdminUsersReturnsPagedResult() throws Exception {
        when(authService.searchAdminUsers(eq("admin"), eq(true), eq("ROLE_ADMIN"), any())).thenReturn(
                new PageResponse<>(
                        List.of(new AdminUserResponse(
                                1L,
                                "admin",
                                "admin@company.com",
                                "System Admin",
                                "Ban dieu hanh",
                                "Administrator",
                                true,
                                List.of("ROLE_ADMIN"),
                                LocalDateTime.of(2026, 5, 3, 9, 0))),
                        1,
                        20,
                        1,
                        1));

        mockMvc.perform(get("/api/auth/admin/users")
                        .param("keyword", "admin")
                        .param("enabled", "true")
                        .param("role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].username").value("admin"))
                .andExpect(jsonPath("$.result.content[0].roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void updateUserAccessReturnsUpdatedUser() throws Exception {
        when(authService.updateAdminUserAccess(eq(7L), any(AdminUpdateUserAccessRequest.class))).thenReturn(
                new AdminUserResponse(
                        7L,
                        "manager",
                        "manager@company.com",
                        "Pham Van Manager",
                        "Van hanh",
                        "Manager",
                        true,
                        List.of("ROLE_MANAGER", "ROLE_EMPLOYEE"),
                        LocalDateTime.of(2026, 5, 3, 9, 0)));

        mockMvc.perform(put("/api/auth/admin/users/7/access")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "roles": ["ROLE_MANAGER", "ROLE_EMPLOYEE"],
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.username").value("manager"))
                .andExpect(jsonPath("$.result.roles[1]").value("ROLE_EMPLOYEE"));
    }
}
