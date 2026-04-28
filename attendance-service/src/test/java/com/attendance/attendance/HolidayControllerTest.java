package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.HolidayController;
import com.attendance.dto.request.HolidayRequest;
import com.attendance.dto.response.HolidayResponse;
import com.attendance.exception.GlobalExceptionHandler;
import com.attendance.service.HolidayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class HolidayControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private HolidayController holidayController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(holidayController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createHolidayReturnsCreatedPayload() throws Exception {
        HolidayRequest request = new HolidayRequest("Giỗ Tổ", LocalDate.of(2026, 4, 18), LocalDate.of(2026, 4, 18), true);
        HolidayResponse response = new HolidayResponse(1L, "Giỗ Tổ", LocalDate.of(2026, 4, 18), LocalDate.of(2026, 4, 18), true);

        when(holidayService.create(any(HolidayRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/attendance/holidays")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.holidayName").value("Giỗ Tổ"));
    }

    @Test
    void createHolidayRejectsBlankName() throws Exception {
        HolidayRequest request = new HolidayRequest("", LocalDate.of(2026, 4, 18), LocalDate.of(2026, 4, 18), true);

        mockMvc.perform(post("/api/attendance/holidays")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
