package com.hr.service;

import com.hr.dto.common.PagingResponse;
import com.hr.dto.employee.EmployeeRequest;
import com.hr.dto.employee.EmployeeResponse;
import com.hr.entity.Department;
import com.hr.entity.Employee;
import com.hr.entity.Position;
import com.hr.exception.AppException;
import com.hr.exception.ErrorCode;
import com.hr.mapper.EmployeeMapper;
import com.hr.repository.DepartmentRepository;
import com.hr.repository.EmployeeRepository;
import com.hr.repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PositionRepository positionRepository,
                           EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên đã tồn tại");
        }

        if (request.email() != null && !request.email().isBlank() && employeeRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Email đã tồn tại");
        }

        Department department = resolveDepartment(request.departmentId());
        Position position = resolvePosition(request.positionId());
        Employee manager = resolveManager(request.managerId());

        Employee employee = employeeMapper.toEntity(request);
        employeeMapper.updateRelations(employee, department, position, manager);

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagingResponse<EmployeeResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EmployeeResponse> resultPage = employeeRepository.findAll(pageable).map(employeeMapper::toResponse);

        return new PagingResponse<>(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.hasNext(),
                resultPage.hasPrevious()
        );
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (!employee.getEmployeeCode().equals(request.employeeCode())
                && employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên đã tồn tại");
        }

        if (request.email() != null && !request.email().isBlank()) {
            boolean duplicatedEmail = employeeRepository.findByEmail(request.email())
                    .filter(found -> !found.getId().equals(employee.getId()))
                    .isPresent();

            if (duplicatedEmail) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Email đã tồn tại");
            }
        }

        Department department = resolveDepartment(request.departmentId());
        Position position = resolvePosition(request.positionId());
        Employee manager = resolveManager(request.managerId());

        employee.setEmployeeCode(request.employeeCode() == null ? null : request.employeeCode().trim());
        employee.setFullName(request.fullName() == null ? null : request.fullName().trim());
        employee.setGender(request.gender());
        employee.setEmail(request.email());
        employee.setStatus(request.status());
        employee.setBiometricHash(request.biometricHash());
        employee.setJoinDate(request.joinDate());
        employeeMapper.updateRelations(employee, department, position, manager);

        Employee saved = employeeRepository.save(employee);
        return employeeMapper.toResponse(saved);
    }



    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    private Position resolvePosition(Long positionId) {
        if (positionId == null) {
            return null;
        }

        return positionRepository.findById(positionId)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
    }

    private Employee resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }

        return employeeRepository.findById(managerId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, "Không tìm thấy quản lý"));
    }
}
