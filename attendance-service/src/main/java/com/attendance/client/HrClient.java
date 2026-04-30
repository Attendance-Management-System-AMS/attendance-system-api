package com.attendance.client;

import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.EmployeeInternalResponse;
import com.attendance.dto.response.FaceMatchResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import java.util.List;
import java.time.LocalDate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hr-service")
public interface HrClient {

    @GetMapping("/internal/hr/users/{userId}/employee")
    EmployeeInternalResponse getEmployeeByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/internal/hr/employees/{employeeId}/snapshot")
    HrEmployeeSnapshot getEmployeeSnapshot(@PathVariable("employeeId") Long employeeId);

    @GetMapping("/internal/hr/employees/{employeeId}/exists")
    boolean employeeExists(@PathVariable("employeeId") Long employeeId);

    @PostMapping("/internal/hr/face-match")
    FaceMatchResponse matchFace(@RequestBody FaceDescriptorRequest request);

    @GetMapping("/internal/hr/employees/snapshots")
    List<HrEmployeeSnapshot> findEmployeeSnapshots(
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "status", required = false) String status);

    @GetMapping("/internal/hr/employees/snapshots")
    List<HrEmployeeSnapshot> findEmployeeSnapshotsByIds(
            @RequestParam("employeeIds") List<Long> employeeIds);

    @GetMapping("/internal/hr/leaves/approved")
    boolean hasApprovedLeave(
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
