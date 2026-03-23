package com.attendance.feign;

import com.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "hr-service", path = "/api")
public interface HrClient {

    @GetMapping("/employees/{id}")
    ApiResponse<Map<String, Object>> getEmployeeById(@PathVariable("id") Long id);
}
