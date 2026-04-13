package com.attendance.service;

import com.attendance.exception.AppException;
import com.attendance.dto.response.PageResponse;
import com.attendance.dto.request.DepartmentRequest;
import com.attendance.dto.response.DepartmentResponse;
import com.attendance.entity.Department;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.DepartmentMapper;
import com.attendance.repository.DepartmentRepository;
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

    // Tìm kiếm phòng ban kèm số lượng nhân viên (sử dụng constructor expression trực tiếp).
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> getList(String keyword, Pageable pageable) {
        return PageResponse.of(departmentRepository.findAllWithCount(keyword, pageable));
    }

    // Lấy phòng ban kèm số lượng nhân viên theo ID.
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return departmentRepository.findByIdWithCount(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
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




