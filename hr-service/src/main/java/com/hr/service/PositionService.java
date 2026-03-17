package com.hr.service;

import com.common.exception.AppException;
import com.hr.dto.position.PositionRequest;
import com.hr.dto.position.PositionResponse;
import com.hr.entity.Department;
import com.hr.entity.Position;
import com.hr.exception.ErrorCode;
import com.hr.mapper.PositionMapper;
import com.hr.repository.DepartmentRepository;
import com.hr.repository.PositionRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionMapper positionMapper;

    public PositionService(PositionRepository positionRepository,
                           DepartmentRepository departmentRepository,
                           PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
        this.positionMapper = positionMapper;
    }

    @Transactional
    public PositionResponse create(PositionRequest request) {
        Department department = resolveDepartment(request.departmentId());

        if (positionRepository.existsByNameIgnoreCaseAndDepartmentId(request.name().trim(), department.getId())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chức vụ đã tồn tại trong phòng ban");
        }

        Position position = positionMapper.toEntity(request, department);
        Position saved = positionRepository.save(position);
        return positionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getAll(Long departmentId) {
        List<Position> positions;
        if (departmentId == null) {
            positions = positionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        } else {
            positions = positionRepository.findByDepartmentId(departmentId)
                    .stream()
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .toList();
        }

        return positions.stream().map(positionMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PositionResponse getById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        return positionMapper.toResponse(position);
    }

    @Transactional
    public PositionResponse update(Long id, PositionRequest request) {
        Position existing = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

        Department department = resolveDepartment(request.departmentId());

        positionRepository.findByNameIgnoreCaseAndDepartmentId(request.name().trim(), department.getId())
                .filter(found -> !found.getId().equals(existing.getId()))
                .ifPresent(found -> {
                    throw new AppException(ErrorCode.INVALID_INPUT, "Chức vụ đã tồn tại trong phòng ban");
                });

        existing.setName(request.name().trim());
        existing.setDepartment(department);
        existing.setLevel(String.valueOf(request.level()));

        Position saved = positionRepository.save(existing);
        return positionMapper.toResponse(saved);
    }

    private Department resolveDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    @Transactional
    public void delete(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        positionRepository.delete(position);
    }
}
