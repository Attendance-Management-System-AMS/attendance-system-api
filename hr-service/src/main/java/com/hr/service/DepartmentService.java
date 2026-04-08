package com.hr.service;

import com.common.exception.AppException;
import com.common.pagination.PageResponse;
import com.hr.dto.department.DepartmentRequest;
import com.hr.dto.department.DepartmentResponse;
import com.hr.entity.Department;
import com.hr.exception.ErrorCode;
import com.hr.mapper.DepartmentMapper;
import com.hr.repository.DepartmentRepository;
import com.hr.repository.DepartmentSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    // Khởi tạo service với repository và mapper của phòng ban.
    public DepartmentService(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    // Tạo mới phòng ban sau khi kiểm tra trùng tên.
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Tên phòng ban đã tồn tại");
        }

        Department department = departmentMapper.toEntity(request);

        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    // Tìm kiếm phòng ban theo từ khoá và phân trang.
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> getList(String keyword, Pageable pageable) {
        var spec = DepartmentSpecifications.matches(keyword);
        Page<Department> page = departmentRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(departmentMapper::toResponse));
    }

    // Lấy phòng ban theo ID.
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        return departmentMapper.toResponse(department);
    }

    // Cập nhật thông tin phòng ban hiện có.
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
        if (request.status() != null) {
            department.setStatus(request.status());
        }
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    // Xóa phòng ban theo ID.
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
        departmentRepository.delete(department);
    }
}
