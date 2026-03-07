package com.hr.mapper;

import com.hr.dto.employee.EmployeeRequest;
import com.hr.dto.employee.EmployeeResponse;
import com.hr.entity.Department;
import com.hr.entity.Employee;
import com.hr.entity.Position;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setEmployeeCode(request.employeeCode() == null ? null : request.employeeCode().trim());
        employee.setFullName(request.fullName() == null ? null : request.fullName().trim());
        employee.setGender(request.gender());
        employee.setEmail(request.email());
        employee.setStatus(request.status());
        employee.setBiometricHash(request.biometricHash());
        employee.setJoinDate(request.joinDate());
        return employee;
    }

    public void updateRelations(Employee employee, Department department, Position position, Employee manager) {
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setManager(manager);
    }

    public EmployeeResponse toResponse(Employee employee) {
        Department department = employee.getDepartment();
        Position position = employee.getPosition();
        Employee manager = employee.getManager();

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getGender(),
                employee.getEmail(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null,
                position != null ? position.getId() : null,
                position != null ? position.getName() : null,
                manager != null ? manager.getId() : null,
                manager != null ? manager.getFullName() : null,
                employee.getStatus(),
                employee.getBiometricHash(),
                employee.getJoinDate(),
                employee.getCreatedAt()
        );
    }
}
