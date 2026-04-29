package com.attendance.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.InternalAuthController;
import com.attendance.dto.response.InternalUserResponse;
import com.attendance.service.AuthService;
import java.util.Set;
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
class InternalAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private InternalAuthController internalAuthController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(internalAuthController)
                .setValidator(validator)
                .build();
    }

    @Test
    void createUserReturnsCreatedInternalUser() throws Exception {
        when(authService.createInternalUser(any())).thenReturn(new InternalUserResponse(
                99L,
                "emp001",
                "emp001@company.com",
                true,
                Set.of("ROLE_EMPLOYEE")));

        mockMvc.perform(post("/internal/auth/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "emp001",
                                  "password": "Emp@1234",
                                  "email": "emp001@company.com",
                                  "enabled": true,
                                  "roles": ["ROLE_EMPLOYEE"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.username").value("emp001"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_EMPLOYEE"));
    }

    @Test
    void updateUserReturnsUpdatedInternalUser() throws Exception {
        when(authService.updateInternalUser(anyLong(), any())).thenReturn(new InternalUserResponse(
                99L,
                "emp001-renamed",
                "emp001-renamed@company.com",
                false,
                Set.of("ROLE_EMPLOYEE")));

        mockMvc.perform(put("/internal/auth/users/99")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "emp001-renamed",
                                  "email": "emp001-renamed@company.com",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.username").value("emp001-renamed"))
                .andExpect(jsonPath("$.enabled").value(false));
    }
}
