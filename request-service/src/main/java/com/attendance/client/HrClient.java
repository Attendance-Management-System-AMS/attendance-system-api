package com.attendance.client;

import com.attendance.dto.response.EmployeeInternalResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hr-service")
public interface HrClient {

    @GetMapping("/internal/hr/users/{userId}/employee")
    EmployeeInternalResponse getEmployeeByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/internal/hr/employees/{employeeId}/snapshot")
    HrEmployeeSnapshot getEmployeeSnapshot(@PathVariable("employeeId") Long employeeId);

    @GetMapping("/internal/hr/employees/{employeeId}/exists")
    boolean employeeExists(@PathVariable("employeeId") Long employeeId);
}
