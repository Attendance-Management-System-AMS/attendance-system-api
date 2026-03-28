package com.attendance.feign;

import com.common.dto.ApiResponse;
import com.common.dto.face.FaceDescriptorRequest;
import com.common.dto.face.FaceMatchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "hr-service", path = "/api")
public interface HrClient {

    @GetMapping("/employees/{id}")
    ApiResponse<Map<String, Object>> getEmployeeById(@PathVariable("id") Long id);

    @PostMapping("/employees/match-face")
    ApiResponse<FaceMatchResponse> matchFace(@RequestBody FaceDescriptorRequest request);
}
