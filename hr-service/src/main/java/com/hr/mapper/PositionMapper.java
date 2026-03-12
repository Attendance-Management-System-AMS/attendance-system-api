package com.hr.mapper;

import com.hr.dto.position.PositionRequest;
import com.hr.dto.position.PositionResponse;
import com.hr.entity.Department;
import com.hr.entity.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public Position toEntity(PositionRequest request, Department department) {
        Position position = new Position();
        position.setName(request.name().trim());
        position.setDepartment(department);
        position.setLevel(String.valueOf(request.level()));
        return position;
    }

    public PositionResponse toResponse(Position position) {
        Department department = position.getDepartment();
        return new PositionResponse(
                position.getId(),
                position.getName(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null,
                parseLevel(position.getLevel())
        );
    }

    private Integer parseLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(level);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
