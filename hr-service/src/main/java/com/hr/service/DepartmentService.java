package com.hr.service;

import com.common.exception.AppException;
import com.hr.dto.department.DepartmentRequest;
import com.hr.dto.department.DepartmentResponse;
import com.hr.entity.Department;
import com.hr.exception.ErrorCode;
import com.hr.mapper.DepartmentMapper;
import com.hr.repository.DepartmentRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Tên phòng ban đã tồn tại");
        }

        Department department = departmentMapper.toEntity(request);

        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        return departmentMapper.toResponse(department);
    }

    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        departmentRepository.findByName(request.name().trim())
                .filter(found -> !found.getId().equals(department.getId()))
                .ifPresent(found -> {
                    throw new AppException(ErrorCode.INVALID_INPUT, "Tên phòng ban đã tồn tại");
                });

        department.setName(request.name().trim());
        department.setDescription(request.description());
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        departmentRepository.delete(department);
    }
}
