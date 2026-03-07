package com.hr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hr")
public class HrHealthController {

    @GetMapping("/ping")
    public String ping() {
        return "Hello world";
    }
}
