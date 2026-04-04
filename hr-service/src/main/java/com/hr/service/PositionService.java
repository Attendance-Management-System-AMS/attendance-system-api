package com.hr.service;

import com.common.exception.AppException;
import com.common.pagination.PageResponse;
import com.hr.dto.position.PositionRequest;
import com.hr.dto.position.PositionResponse;
import com.hr.entity.Department;
import com.hr.entity.Position;
import com.hr.exception.ErrorCode;
import com.hr.mapper.PositionMapper;
import com.hr.repository.DepartmentRepository;
import com.hr.repository.PositionRepository;
import com.hr.repository.PositionSpecifications;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionMapper positionMapper;

    // Khởi tạo service với repository và mapper cho chức vụ.
    public PositionService(PositionRepository positionRepository,
                           DepartmentRepository departmentRepository,
                           PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.departmentRepository = departmentRepository;
        this.positionMapper = positionMapper;
    }

    // Tạo chức vụ mới trong một phòng ban.
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

    // Tìm kiếm chức vụ theo từ khoá và phòng ban.
    @Transactional(readOnly = true)
    public PageResponse<PositionResponse> search(String keyword, Long departmentId, Pageable pageable) {
        var spec = PositionSpecifications.matches(keyword, departmentId);
        Page<Position> page = positionRepository.findAll(spec, pageable);
        List<PositionResponse> content = page.getContent().stream()
                .map(positionMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    // Lấy chức vụ theo ID.
    @Transactional(readOnly = true)
    public PositionResponse getById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        return positionMapper.toResponse(position);
    }

    // Cập nhật thông tin chức vụ hiện có.
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

    // Lấy phòng ban theo ID để gắn cho chức vụ.
    private Department resolveDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    // Xoá chức vụ theo ID.
    @Transactional
    public void delete(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        positionRepository.delete(position);
    }
}
