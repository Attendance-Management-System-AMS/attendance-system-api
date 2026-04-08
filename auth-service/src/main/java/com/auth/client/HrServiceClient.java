package com.auth.client;

import com.common.dto.EmployeeInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hr-service")
public interface HrServiceClient {

    @GetMapping("/api/employees/internal/user/{userId}")
    EmployeeInternalResponse getInternalEmployee(@PathVariable("userId") Long userId);
}
