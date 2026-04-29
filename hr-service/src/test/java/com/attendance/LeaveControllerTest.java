package com.attendance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.common.dto.PageResponse;
import com.attendance.controller.LeaveController;
import com.attendance.dto.request.LeaveRequestRecord;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.common.error.GlobalExceptionHandler;
import com.attendance.service.EmployeeService;
import com.attendance.service.LeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class LeaveControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private LeaveService leaveService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private LeaveController leaveController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(leaveController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getMyLeavesReturnsPagedResult() throws Exception {
        LeaveResponse leave = new LeaveResponse(1L, 4L, "Pham Thi Employee", "EMP-0004", "Kinh doanh", "Nhân viên",
                "AL", "Nghỉ phép năm", LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10), 1.0, "Việc riêng", "PENDING", null, LocalDateTime.now(), null, null);
        PageResponse<LeaveResponse> page = new PageResponse<>(List.of(leave), 1, 20, 1, 1);

        when(employeeService.getCurrentEmployeeId()).thenReturn(4L);
        when(leaveService.search(eq(null), eq(4L), eq(null), any())).thenReturn(page);

        mockMvc.perform(get("/api/leaves/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].employeeId").value(4));
    }

    @Test
    void createMyLeaveReturnsCreatedPayload() throws Exception {
        LeaveRequestRecord request = new LeaveRequestRecord(null, "AL",
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10), 1.0, "Việc riêng", null, null);
        LeaveResponse response = new LeaveResponse(1L, 4L, "Pham Thi Employee", "EMP-0004", "Kinh doanh", "Nhân viên",
                "AL", "Nghỉ phép năm", LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10), 1.0, "Việc riêng", "PENDING", null, LocalDateTime.now(), null, null);

        when(employeeService.getCurrentEmployeeId()).thenReturn(4L);
        when(leaveService.createRequest(any(LeaveRequestRecord.class))).thenReturn(response);

        mockMvc.perform(post("/api/leaves/me")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result.status").value("PENDING"));
    }

    @Test
    void getLeaveTypesReturnsList() throws Exception {
        when(leaveService.getAllLeaveTypes()).thenReturn(List.of(
                new LeaveTypeResponse(1L, "AL", "Nghỉ phép năm", true, true, "Có lương")));

        mockMvc.perform(get("/api/leaves/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].code").value("AL"));
    }

    @Test
    void approveLeaveReturnsUpdatedStatus() throws Exception {
        LeaveResponse response = new LeaveResponse(1L, 4L, "Pham Thi Employee", "EMP-0004", "Kinh doanh", "Nhân viên",
                "AL", "Nghỉ phép năm", LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10), 1.0, "Việc riêng", "APPROVED", "Tran Thi HR", LocalDateTime.now(), null, null);

        when(employeeService.getCurrentEmployeeId()).thenReturn(2L);
        when(leaveService.approve(1L, 2L)).thenReturn(response);

        mockMvc.perform(put("/api/leaves/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("APPROVED"));
    }

    @Test
    void createLeaveRejectsMissingType() throws Exception {
        LeaveRequestRecord request = new LeaveRequestRecord(null, "",
                LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 10), 1.0, "Việc riêng", null, null);

        mockMvc.perform(post("/api/leaves/me")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
