package com.attendance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.AttendanceResponse;
import com.attendance.service.AttendanceService;
import com.attendance.service.CurrentUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private CurrentUserService currentUserService;

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
