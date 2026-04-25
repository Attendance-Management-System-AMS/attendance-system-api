package com.attendance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/internal/auth/tokens/blacklisted")
    boolean isTokenBlacklisted(@RequestParam("jti") String jti);
}
