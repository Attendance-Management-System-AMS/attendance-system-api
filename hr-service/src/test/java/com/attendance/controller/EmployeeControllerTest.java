package com.attendance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.dto.request.EmployeeRequest;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.EmployeeResponse;
import com.attendance.service.EmployeeService;
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

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void createEmployeeReturnsCreatedPayload() throws Exception {
        EmployeeRequest request = new EmployeeRequest(
                "EMP-QA-01",
                "Nguyen Van QA",
                "MALE",
                "qa@company.com",
                3L,
                4L,
                3L,
                "ACTIVE",
                null,
                LocalDate.of(2026, 4, 1));

        EmployeeResponse response = new EmployeeResponse(
                11L, null, "EMP-QA-01", "Nguyen Van QA", "MALE", "qa@company.com",
                3L, "Phòng IT", 4L, "Tester", 3L, "Le Van Manager",
                "ACTIVE", false, LocalDate.of(2026, 4, 1), LocalDateTime.now());

        when(employeeService.create(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result.employeeCode").value("EMP-QA-01"));
    }

    @Test
    void registerFaceDescriptorRejectsInvalidLength() throws Exception {
        FaceDescriptorRequest request = new FaceDescriptorRequest(List.of(0.1d, 0.2d));

        mockMvc.perform(put("/api/employees/1/face-descriptor")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
