package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.common.dto.PageResponse;
import com.attendance.controller.ShiftController;
import com.attendance.dto.request.ShiftRequest;
import com.attendance.dto.response.ShiftResponse;
import com.attendance.exception.GlobalExceptionHandler;
import com.attendance.service.ShiftService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
class ShiftControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private ShiftController shiftController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(shiftController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createShiftReturnsCreatedPayload() throws Exception {
        ShiftRequest request = new ShiftRequest("Ca sáng", LocalTime.of(8, 0), LocalTime.of(17, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0), 10);
        ShiftResponse response = new ShiftResponse(1L, "Ca sáng", LocalTime.of(8, 0), LocalTime.of(17, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0), 10, LocalDateTime.now());

        when(shiftService.create(any(ShiftRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/attendance/shifts")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Ca sáng"));
    }

    @Test
    void listShiftsReturnsPagedResult() throws Exception {
        ShiftResponse item = new ShiftResponse(1L, "Ca sáng", LocalTime.of(8, 0), LocalTime.of(17, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0), 10, LocalDateTime.now());
        PageResponse<ShiftResponse> page = new PageResponse<>(List.of(item), 1, 20, 1, 1);

        when(shiftService.search(eq("sáng"), any())).thenReturn(page);

        mockMvc.perform(get("/api/attendance/shifts").param("keyword", "sáng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].name").value("Ca sáng"));
    }

    @Test
    void createShiftRejectsBlankName() throws Exception {
        ShiftRequest request = new ShiftRequest("", LocalTime.of(8, 0), LocalTime.of(17, 0),
                null, null, 5);

        mockMvc.perform(post("/api/attendance/shifts")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
