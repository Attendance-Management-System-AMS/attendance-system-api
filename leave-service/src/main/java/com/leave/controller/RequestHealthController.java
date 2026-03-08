package com.request.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/request")
public class RequestHealthController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("service", "request-service", "status", "ok");
    }
}
