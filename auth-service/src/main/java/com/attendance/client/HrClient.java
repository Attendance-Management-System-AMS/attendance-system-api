package com.attendance.client;

import com.attendance.dto.response.EmployeeInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hr-service")
public interface HrClient {

    @GetMapping("/internal/hr/users/{userId}/employee")
    EmployeeInternalResponse getEmployeeByUserId(@PathVariable("userId") Long userId);
}
