package com.hr.service;

import com.hr.dto.department.DepartmentRequest;
import com.hr.dto.department.DepartmentResponse;
import com.common.exception.AppException;
import com.hr.entity.Department;
import com.hr.exception.ErrorCode;
import com.hr.mapper.DepartmentMapper;
import com.hr.repository.DepartmentRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }
}
