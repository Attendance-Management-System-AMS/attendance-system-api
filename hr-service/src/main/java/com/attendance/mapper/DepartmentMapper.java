package com.attendance.mapper;

import com.attendance.dto.request.DepartmentRequest;
import com.attendance.dto.response.DepartmentResponse;
import com.attendance.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    // Chuyển request tạo phòng ban sang entity.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Department toEntity(DepartmentRequest request);

    // Chuyển entity phòng ban sang response.
    DepartmentResponse toResponse(Department department);

    // Cập nhật entity phòng ban từ request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(DepartmentRequest request, @MappingTarget Department department);
}




