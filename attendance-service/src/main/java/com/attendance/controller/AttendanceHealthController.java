package com.attendance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Chấm công - Health", description = "Kiểm tra trạng thái hoạt động của service")
public class AttendanceHealthController {

    @GetMapping("/ping")
    @Operation(summary = "Kiểm tra service còn hoạt động hay không")
    public Map<String, String> ping() {
        return Map.of("service", "attendance-service", "status", "ok");
    }
}
