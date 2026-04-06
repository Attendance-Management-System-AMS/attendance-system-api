package com.hr.service;

import com.common.dto.face.FaceDescriptorRequest;
import com.common.dto.face.FaceMatchResponse;
import com.common.exception.AppException;
import com.common.pagination.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr.dto.employee.EmployeeRequest;
import com.hr.dto.employee.EmployeeResponse;
import com.hr.entity.Department;
import com.hr.entity.Employee;
import com.hr.entity.Position;
import com.hr.exception.ErrorCode;
import com.hr.mapper.EmployeeMapper;
import com.hr.repository.DepartmentRepository;
import com.hr.repository.EmployeeRepository;
import com.hr.repository.EmployeeSpecifications;
import com.hr.repository.PositionRepository;
import com.hr.util.FaceEmbeddingUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;
    private final ObjectMapper objectMapper;
    private final double faceMatchDistanceThreshold;

    // Khởi tạo service với các dependency xử lý nhân viên và khuôn mặt.
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PositionRepository positionRepository,
                           EmployeeMapper employeeMapper,
                           ObjectMapper objectMapper,
                           @Value("${app.face-match.distance-threshold:0.55}") double faceMatchDistanceThreshold) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeMapper = employeeMapper;
        this.objectMapper = objectMapper;
        this.faceMatchDistanceThreshold = faceMatchDistanceThreshold;
    }

    // Tạo mới hồ sơ nhân viên sau khi kiểm tra trùng mã và email.
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

    // Tìm kiếm nhân viên theo nhiều điều kiện và phân trang.
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(
            String keyword,
            Long departmentId,
            Long positionId,
            String status,
            Pageable pageable) {
        var spec = EmployeeSpecifications.matches(keyword, departmentId, positionId, status);
        Page<Employee> slice = employeeRepository.findAll(spec, pageable);
        return PageResponse.of(slice.map(employeeMapper::toResponse));
    }

    // Lấy chi tiết nhân viên theo ID.
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return employeeMapper.toResponse(employee);
    }

    // Cập nhật thông tin nhân viên hiện có.
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

    // Lấy phòng ban theo ID, hoặc trả về null nếu không truyền.
    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    // Lấy chức vụ theo ID, hoặc trả về null nếu không truyền.
    private Position resolvePosition(Long positionId) {
        if (positionId == null) {
            return null;
        }

        return positionRepository.findById(positionId)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
    }

    // Lấy quản lý theo ID, hoặc trả về null nếu không truyền.
    private Employee resolveManager(Long managerId) {
        if (managerId == null) {
            return null;
        }

        return employeeRepository.findById(managerId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, "Không tìm thấy quản lý"));
    }

    // Chuyển trạng thái nhân viên sang INACTIVE thay vì xoá cứng.
    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        employee.setStatus("INACTIVE");
        employeeRepository.save(employee);
    }

    /**
     * Lưu descriptor face-api.js (128 float JSON) cho nhân viên — dùng khi đăng ký khuôn mặt từ FE.
     */
    @Transactional
    public EmployeeResponse registerFaceEmbedding(Long id, FaceDescriptorRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        double[] probe = FaceEmbeddingUtils.toDoubleArray(request.descriptor());
        if (probe.length != FaceEmbeddingUtils.FACE_DESCRIPTOR_LENGTH) {
            throw new AppException(ErrorCode.INVALID_INPUT, "descriptor phải có đúng 128 phần tử");
        }
        try {
            employee.setFaceEmbedding(objectMapper.writeValueAsString(request.descriptor()));
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không lưu được descriptor");
        }
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    /**
     * So khớp với nhân viên ACTIVE đã có face_embedding; Euclidean distance nhỏ hơn ngưỡng thì khớp
     * (cùng metric với {@code faceapi.euclideanDistance} trong face-api.js).
     */
    @Transactional(readOnly = true)
    public FaceMatchResponse matchFace(FaceDescriptorRequest request) {
        double[] probe = FaceEmbeddingUtils.toDoubleArray(request.descriptor());
        if (probe.length != FaceEmbeddingUtils.FACE_DESCRIPTOR_LENGTH) {
            throw new AppException(ErrorCode.INVALID_INPUT, "descriptor phải có đúng 128 phần tử");
        }
        List<Employee> candidates = employeeRepository.findByStatusAndFaceEmbeddingIsNotNull("ACTIVE");
        if (candidates.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chưa có nhân viên nào đăng ký khuôn mặt");
        }
        double best = Double.MAX_VALUE;
        Employee bestEmp = null;
        for (Employee e : candidates) {
            try {
                double[] stored = FaceEmbeddingUtils.fromJson(e.getFaceEmbedding(), objectMapper);
                if (stored.length != FaceEmbeddingUtils.FACE_DESCRIPTOR_LENGTH) {
                    continue;
                }
                double d = FaceEmbeddingUtils.euclideanDistance(probe, stored);
                if (d < best) {
                    best = d;
                    bestEmp = e;
                }
            } catch (JsonProcessingException ex) {
                // bỏ qua bản ghi không parse được
            }
        }
        if (bestEmp == null || best > faceMatchDistanceThreshold) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không nhận diện được nhân viên (khoảng cách vượt ngưỡng)");
        }
        return new FaceMatchResponse(bestEmp.getId(), best);
    }
}
