package com.hr.mapper;

import com.hr.dto.department.DepartmentRequest;
import com.hr.dto.department.DepartmentResponse;
import com.hr.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name().trim());
        department.setDescription(request.description());
        return department;
    }

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt()
        );
    }
}
