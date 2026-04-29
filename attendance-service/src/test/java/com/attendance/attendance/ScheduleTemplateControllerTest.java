package com.attendance.attendance;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.attendance.controller.ScheduleTemplateController;
import com.attendance.dto.request.ScheduleTemplateItemRequest;
import com.attendance.dto.request.ScheduleTemplateRequest;
import com.attendance.dto.response.ScheduleTemplateResponse;
import com.attendance.common.error.GlobalExceptionHandler;
import com.attendance.service.ScheduleTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ScheduleTemplateControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private ScheduleTemplateService scheduleTemplateService;

    @InjectMocks
    private ScheduleTemplateController scheduleTemplateController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleTemplateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createTemplateReturnsCreatedPayload() throws Exception {
        ScheduleTemplateRequest request = new ScheduleTemplateRequest("Lịch hành chính", "Thứ 2-6", List.of(new ScheduleTemplateItemRequest(1, 1L)));
        ScheduleTemplateResponse response = new ScheduleTemplateResponse(1L, "Lịch hành chính", "Thứ 2-6", List.of());

        when(scheduleTemplateService.create(any(ScheduleTemplateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/attendance/schedule-templates")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Lịch hành chính"));
    }

    @Test
    void getAllTemplatesReturnsList() throws Exception {
        when(scheduleTemplateService.getAll()).thenReturn(List.of(new ScheduleTemplateResponse(1L, "Lịch hành chính", "Thứ 2-6", List.of())));

        mockMvc.perform(get("/api/attendance/schedule-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].name").value("Lịch hành chính"));
    }
}
