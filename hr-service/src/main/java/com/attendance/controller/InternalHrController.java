package com.attendance.controller;

import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.EmployeeInternalResponse;
import com.attendance.dto.response.FaceMatchResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/hr")
@RequiredArgsConstructor
public class InternalHrController {

    private final EmployeeService employeeService;

    @GetMapping("/users/{userId}/employee")
    public EmployeeInternalResponse getEmployeeByUserId(@PathVariable Long userId) {
        return employeeService.getInternalEmployee(userId);
    }

    @GetMapping("/users/employees")
    public List<EmployeeInternalResponse> getEmployeesByUserIds(@RequestParam List<Long> userIds) {
        return employeeService.getInternalEmployees(userIds);
    }

    @GetMapping("/employees/{employeeId}/snapshot")
    public HrEmployeeSnapshot getEmployeeSnapshot(@PathVariable Long employeeId) {
        return employeeService.getEmployeeSnapshot(employeeId);
    }

    @GetMapping("/employees/{employeeId}/exists")
    public boolean employeeExists(@PathVariable Long employeeId) {
        return employeeService.getEmployeeSnapshot(employeeId) != null;
    }

    @GetMapping("/employees/snapshots")
    public List<HrEmployeeSnapshot> findEmployeeSnapshots(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<Long> employeeIds) {
        return employeeService.findEmployeeSnapshots(departmentId, employeeId, status, employeeIds);
    }

    @PostMapping("/face-match")
    public FaceMatchResponse matchFace(@Valid @RequestBody FaceDescriptorRequest request) {
        return employeeService.matchFace(request);
    }
}
