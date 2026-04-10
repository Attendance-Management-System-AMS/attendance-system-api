package com.attendance.mapper;

import com.attendance.dto.request.EmployeeRequest;
import com.attendance.dto.response.EmployeeResponse;
import com.attendance.entity.Department;
import com.attendance.entity.Employee;
import com.attendance.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface EmployeeMapper {

    // Chuyển request tạo nhân viên sang entity.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Employee toEntity(EmployeeRequest request);

    // Chuyển entity nhân viên sang response đầy đủ quan hệ.
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "position.id", target = "positionId")
    @Mapping(source = "position.name", target = "positionName")
    @Mapping(source = "manager.id", target = "managerId")
    @Mapping(source = "manager.fullName", target = "managerName")
    @Mapping(target = "faceRegistered", expression = "java(employee.getFaceEmbedding() != null && !employee.getFaceEmbedding().isBlank())")
    EmployeeResponse toResponse(Employee employee);

    // Cập nhật entity nhân viên từ request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(EmployeeRequest request, @MappingTarget Employee employee);

    // Gán quan hệ phòng ban, chức vụ và quản lý cho nhân viên.
    default void updateRelations(Employee employee, Department department, Position position, Employee manager) {
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setManager(manager);
    }
}




