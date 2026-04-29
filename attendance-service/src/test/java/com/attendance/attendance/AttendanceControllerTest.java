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
import com.attendance.controller.AttendanceController;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.common.error.GlobalExceptionHandler;
import com.attendance.service.AttendanceService;
import com.attendance.service.CurrentUserService;
import com.attendance.service.KioskAccessService;
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
class AttendanceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private KioskAccessService kioskAccessService;

    @InjectMocks
    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void checkInReturnsAttendanceRecord() throws Exception {
        AttendanceResponse response = new AttendanceResponse(
                1L, 4L, LocalDateTime.now(), null, LocalDate.now(), "PRESENT",
                0, 0, 0, 480, LocalDateTime.now(), "Pham Thi Employee", "EMP-EMPLOYEE", "Phòng IT", "Lập Trình Viên");

        when(attendanceService.checkIn(4L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/check-in/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.employeeId").value(4))
                .andExpect(jsonPath("$.result.status").value("PRESENT"));
    }

    @Test
    void scanByFaceRejectsInvalidDescriptorLength() throws Exception {
        FaceDescriptorRequest request = new FaceDescriptorRequest(List.of(0.1d));

        mockMvc.perform(post("/api/attendance/scan-by-face")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void scanByFaceRequiresValidatedKioskSession() throws Exception {
        FaceDescriptorRequest request = new FaceDescriptorRequest(java.util.Collections.nCopies(128, 0.1d));
        AttendanceResponse response = new AttendanceResponse(
                1L, 4L, LocalDateTime.now(), null, LocalDate.now(), "PRESENT",
                0, 0, 0, 480, LocalDateTime.now(), "Pham Thi Employee", "EMP-EMPLOYEE", "Phòng IT", "Lập Trình Viên");

        when(kioskAccessService.validateScanRequest("session-token", "kiosk-a", "nonce-1", "1745587200000"))
                .thenReturn("kiosk-a");
        when(attendanceService.scanByFace(any(FaceDescriptorRequest.class), eq("kiosk-a"))).thenReturn(response);

        mockMvc.perform(post("/api/attendance/scan-by-face")
                        .header(KioskAccessService.HEADER_SESSION, "session-token")
                        .header(KioskAccessService.HEADER_DEVICE_ID, "kiosk-a")
                        .header(KioskAccessService.HEADER_NONCE, "nonce-1")
                        .header(KioskAccessService.HEADER_TIMESTAMP, "1745587200000")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.employeeId").value(4))
                .andExpect(jsonPath("$.result.status").value("PRESENT"));
    }

    @Test
    void getMyAttendanceReturnsPagedResult() throws Exception {
        AttendanceResponse item = new AttendanceResponse(
                1L, 4L, LocalDateTime.now(), LocalDateTime.now(), LocalDate.now(), "PRESENT",
                0, 0, 480, 480, LocalDateTime.now(), "Pham Thi Employee", "EMP-EMPLOYEE", "Phòng IT", "Lập Trình Viên");
        PageResponse<AttendanceResponse> page = new PageResponse<>(List.of(item), 1, 20, 1, 1);

        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(attendanceService.search(eq(4L), any(), any(), any(), eq(null), any())).thenReturn(page);

        mockMvc.perform(get("/api/attendance/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].employeeId").value(4))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }
}
