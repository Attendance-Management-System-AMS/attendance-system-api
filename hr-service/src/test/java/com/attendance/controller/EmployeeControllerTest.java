package com.attendance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.dto.request.CreateEmployeeRequest;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.EmployeeResponse;
import com.attendance.common.error.GlobalExceptionHandler;
import com.attendance.service.EmployeeService;
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
class EmployeeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createEmployeeReturnsCreatedPayload() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
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

        when(employeeService.create(any(CreateEmployeeRequest.class))).thenReturn(response);

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
