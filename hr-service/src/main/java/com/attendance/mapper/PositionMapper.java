package com.attendance.mapper;

import com.attendance.dto.request.PositionRequest;
import com.attendance.dto.response.PositionResponse;
import com.attendance.entity.Department;
import com.attendance.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PositionMapper {

    // Chuyển request tạo chức vụ sang entity.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPosition", ignore = true)
    @Mapping(target = "department", source = "department")
    @Mapping(target = "level", source = "request.level", qualifiedByName = "normalizeLevel")
    @Mapping(target = "name", source = "request.name")
    Position toEntity(PositionRequest request, Department department);

    // Chuyển entity chức vụ sang response.
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "level", target = "level", qualifiedByName = "levelToResponse")
    PositionResponse toResponse(Position position);

    // Cập nhật entity chức vụ từ request.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPosition", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "level", source = "level", qualifiedByName = "normalizeLevel")
    void updateEntity(PositionRequest request, @MappingTarget Position position);

    // Lưu level theo định dạng thống nhất, đồng thời chấp nhận payload cũ dạng số.
    @Named("normalizeLevel")
    default String normalizeLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.matches("\\d+")) {
            return "LEVEL_" + normalized;
        }
        return normalized;
    }

    // Trả về số để các form hiện tại dùng input number mà vẫn tương thích dữ liệu LEVEL_n.
    @Named("levelToResponse")
    default String levelToResponse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.startsWith("LEVEL_")) {
            return normalized.substring("LEVEL_".length());
        }
        return normalized;
    }
}




