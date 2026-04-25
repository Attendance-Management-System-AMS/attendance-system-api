package com.attendance.auth;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.InternalAuthController;
import com.attendance.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class InternalAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private InternalAuthController internalAuthController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(internalAuthController).build();
    }

    @Test
    void tokenBlacklistEndpointReturnsBoolean() throws Exception {
        when(authService.isTokenBlacklisted("revoked-jti")).thenReturn(true);

        mockMvc.perform(get("/internal/auth/tokens/blacklisted").param("jti", "revoked-jti"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
