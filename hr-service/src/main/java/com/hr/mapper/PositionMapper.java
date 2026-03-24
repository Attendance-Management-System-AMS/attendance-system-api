package com.hr.mapper;

import com.hr.dto.position.PositionRequest;
import com.hr.dto.position.PositionResponse;
import com.hr.entity.Department;
import com.hr.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapStructConfig.class)
public interface PositionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPosition", ignore = true)
    @Mapping(target = "department", source = "department")
    @Mapping(target = "level", source = "request.level", qualifiedByName = "intToString")
    @Mapping(target = "name", source = "request.name")
    Position toEntity(PositionRequest request, Department department);

    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "level", target = "level", qualifiedByName = "stringToInt")
    PositionResponse toResponse(Position position);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentPosition", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "level", source = "level", qualifiedByName = "intToString")
    void updateEntity(PositionRequest request, @MappingTarget Position position);

    @Named("intToString")
    default String intToString(Integer value) {
        return value == null ? null : String.valueOf(value);
    }

    @Named("stringToInt")
    default Integer stringToInt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
