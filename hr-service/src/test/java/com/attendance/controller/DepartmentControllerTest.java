package com.attendance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.DepartmentRequest;
import com.attendance.dto.response.DepartmentResponse;
import com.attendance.service.DepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void createDepartmentReturnsCreatedPayload() throws Exception {
        DepartmentRequest request = new DepartmentRequest("Phòng QA", "Kiểm thử phần mềm", "ACTIVE");
        DepartmentResponse response = new DepartmentResponse(10L, "Phòng QA", "Kiểm thử phần mềm", "ACTIVE", 0L, LocalDateTime.now());

        when(departmentService.create(any(DepartmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/departments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result.name").value("Phòng QA"));
    }

    @Test
    void createDepartmentRejectsBlankName() throws Exception {
        DepartmentRequest request = new DepartmentRequest("", "desc", "ACTIVE");

        mockMvc.perform(post("/api/departments")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDepartmentsReturnsPagedData() throws Exception {
        DepartmentResponse department = new DepartmentResponse(1L, "Phòng IT", "CNTT", "ACTIVE", 5L, LocalDateTime.now());
        PageResponse<DepartmentResponse> page = new PageResponse<>(List.of(department), 1, 20, 1, 1);

        when(departmentService.getList(eq("IT"), any())).thenReturn(page);

        mockMvc.perform(get("/api/departments").param("keyword", "IT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].name").value("Phòng IT"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void deleteDepartmentReturnsSuccessMessage() throws Exception {
        doNothing().when(departmentService).delete(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa phòng ban thành công"));
    }
}
