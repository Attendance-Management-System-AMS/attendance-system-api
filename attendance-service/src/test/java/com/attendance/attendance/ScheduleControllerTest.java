package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.ScheduleController;
import com.attendance.dto.request.BulkScheduleRequest;
import com.attendance.dto.request.EmployeeScheduleRequest;
import com.attendance.dto.response.EmployeeScheduleResponse;
import com.attendance.exception.GlobalExceptionHandler;
import com.attendance.service.CurrentUserService;
import com.attendance.service.ScheduleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class ScheduleControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ScheduleController scheduleController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void assignScheduleReturnsCreatedPayload() throws Exception {
        EmployeeScheduleRequest request = new EmployeeScheduleRequest(
                4L,
                1L,
                1,
                true,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                false);
        EmployeeScheduleResponse response = new EmployeeScheduleResponse(
                1L, 4L, "Pham Thi Employee", 1L, "Ca sáng",
                "08:00", "17:30", 1, true, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        when(scheduleService.assignSchedule(any(EmployeeScheduleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/attendance/schedules")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.employeeId").value(4))
                .andExpect(jsonPath("$.result.shiftName").value("Ca sáng"));
    }

    @Test
    void bulkAssignReturnsList() throws Exception {
        BulkScheduleRequest request = new BulkScheduleRequest(
                List.of(4L),
                1L,
                List.of(1, 2, 3, 4, 5),
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30),
                false);
        EmployeeScheduleResponse response = new EmployeeScheduleResponse(
                1L, 4L, "Pham Thi Employee", 1L, "Ca sáng",
                "08:00", "17:30", 1, true, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        when(scheduleService.bulkAssign(any(BulkScheduleRequest.class))).thenReturn(List.of(response));

        mockMvc.perform(post("/api/attendance/schedules/bulk")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].employeeId").value(4));
    }

    @Test
    void getMyScheduleUsesCurrentEmployeeId() throws Exception {
        EmployeeScheduleResponse response = new EmployeeScheduleResponse(
                1L, 4L, "Pham Thi Employee", 1L, "Ca sáng",
                "08:00", "17:30", 1, true, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(scheduleService.getByEmployee(eq(4L))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/attendance/schedules/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].employeeId").value(4))
                .andExpect(jsonPath("$.result[0].startTime").value("08:00"))
                .andExpect(jsonPath("$.result[0].endTime").value("17:30"));
    }
}
