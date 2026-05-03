package com.attendance.client;

import com.attendance.dto.response.EmployeeInternalResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hr-service")
public interface HrClient {

    @GetMapping("/internal/hr/users/{userId}/employee")
    EmployeeInternalResponse getEmployeeByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/internal/hr/users/employees")
    List<EmployeeInternalResponse> getEmployeesByUserIds(@RequestParam("userIds") List<Long> userIds);
}
