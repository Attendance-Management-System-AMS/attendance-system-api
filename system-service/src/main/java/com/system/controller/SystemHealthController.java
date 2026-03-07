package com.system.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("service", "system-service", "status", "ok");
    }
}
