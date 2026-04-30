package com.attendance.service;

import com.attendance.client.AuthClient;
import com.attendance.dto.request.FaceDescriptorRequest;
import com.attendance.dto.response.FaceMatchResponse;
import com.attendance.exception.AppException;
import com.attendance.common.dto.PageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.attendance.dto.request.CreateEmployeeRequest;
import com.attendance.dto.request.InternalCreateUserRequest;
import com.attendance.dto.request.InternalUpdateUserRequest;
import com.attendance.dto.response.EmployeeResponse;
import com.attendance.entity.Department;
import com.attendance.entity.Employee;
import com.attendance.entity.Position;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.EmployeeMapper;
import com.attendance.dto.response.InternalUserResponse;
import com.attendance.dto.request.UpdateEmployeeRequest;
import com.attendance.dto.response.EmployeeInternalResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.spec.EmployeeSpecifications;
import com.attendance.repository.PositionRepository;
import com.attendance.util.FaceEmbeddingUtils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;
    private final ObjectMapper objectMapper;
    private final AuthClient authClient;
    private final double faceMatchDistanceThreshold;

    // Khởi tạo service với các dependency xử lý nhân viên và khuôn mặt.
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PositionRepository positionRepository,
                           EmployeeMapper employeeMapper,
                           ObjectMapper objectMapper,
                           AuthClient authClient,
                           @Value("${app.face-match.distance-threshold:0.55}") double faceMatchDistanceThreshold) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeMapper = employeeMapper;
        this.objectMapper = objectMapper;
        this.authClient = authClient;
        this.faceMatchDistanceThreshold = faceMatchDistanceThreshold;
    }

    // Lấy ID nhân viên hiện tại đang đăng nhập.
    public Long getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        String identifier = authentication.getName();
        if (identifier == null || !identifier.chars().allMatch(Character::isDigit)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token phải chứa userId dạng số");
        }

        Long userId = Long.parseLong(identifier);
        return employeeRepository.findByUserId(userId)
                .map(Employee::getId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, "Không tìm thấy hồ sơ nhân viên cho tài khoản này"));
    }

    // Tạo mới hồ sơ nhân viên sau khi kiểm tra trùng mã và email.
    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (request.email() != null && !request.email().isBlank() && employeeRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Email đã tồn tại");
        }

        Department department = resolveDepartment(request.departmentId());
        Position position = resolvePosition(request.positionId());
        Employee manager = resolveManager(request.managerId());

        Employee employee = employeeMapper.toEntity(request);
        employee.setEmployeeCode(createPendingEmployeeCode());
        if (employee.getStatus() == null || employee.getStatus().isBlank()) {
            employee.setStatus("ACTIVE");
        }
        employeeMapper.updateRelations(employee, department, position, manager);

        Employee saved = employeeRepository.save(employee);
        saved.setEmployeeCode(formatEmployeeCode(saved.getId()));
        InternalUserResponse user = authClient.createUser(new InternalCreateUserRequest(
                saved.getEmployeeCode(),
                buildDefaultPassword(saved.getEmployeeCode()),
                saved.getEmail(),
                isActiveStatus(saved.getStatus()),
                Set.of("ROLE_EMPLOYEE")));
        saved.setUserId(user.id());
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

    // Phuong thuc moi cho Auth module goi truc tiep
    @Transactional(readOnly = true)
    public EmployeeInternalResponse getInternalEmployee(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId).orElse(null);
        if (employee == null) return null;
        
        return EmployeeInternalResponse.builder()
                .employeeId(employee.getId())
                .userId(employee.getUserId())
                .fullName(employee.getFullName())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .positionName(employee.getPosition() != null ? employee.getPosition().getName() : null)
                .build();
    }

    // Phuong thuc moi cho Attendance module goi truc tiep
    @Transactional(readOnly = true)
    public HrEmployeeSnapshot getEmployeeSnapshot(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) return null;

        return toSnapshot(employee);
    }

    @Transactional(readOnly = true)
    public List<HrEmployeeSnapshot> findEmployeeSnapshots(Long departmentId, Long employeeId, String status) {
        return findEmployeeSnapshots(departmentId, employeeId, status, null);
    }

    @Transactional(readOnly = true)
    public List<HrEmployeeSnapshot> findEmployeeSnapshots(
            Long departmentId,
            Long employeeId,
            String status,
            List<Long> employeeIds) {
        if (employeeIds != null && !employeeIds.isEmpty()) {
            return employeeRepository.findByIdIn(employeeIds).stream()
                    .map(this::toSnapshot)
                    .toList();
        }

        if (employeeId != null) {
            return employeeRepository.findById(employeeId).stream()
                    .map(this::toSnapshot)
                    .toList();
        }

        var spec = EmployeeSpecifications.matches(null, departmentId, null, status);
        return employeeRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "fullName"))
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    private HrEmployeeSnapshot toSnapshot(Employee employee) {
        return new HrEmployeeSnapshot(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getPosition() != null ? employee.getPosition().getName() : null);
    }

    // Cập nhật thông tin nhân viên hiện có.
    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        String normalizedEmployeeCode = request.employeeCode() == null ? null : request.employeeCode().trim();
        if (normalizedEmployeeCode == null || normalizedEmployeeCode.isBlank()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên là bắt buộc");
        }

        if (!employee.getEmployeeCode().equals(normalizedEmployeeCode)
                && employeeRepository.existsByEmployeeCode(normalizedEmployeeCode)) {
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

        employee.setEmployeeCode(normalizedEmployeeCode);
        employee.setFullName(request.fullName() == null ? null : request.fullName().trim());
        employee.setGender(request.gender());
        employee.setEmail(request.email() == null ? null : request.email().trim());
        employee.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status().trim());
        employee.setBiometricHash(request.biometricHash());
        employee.setJoinDate(request.joinDate());
        employeeMapper.updateRelations(employee, department, position, manager);

        Employee saved = employeeRepository.save(employee);
        syncAuthAccount(saved);
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

    private String createPendingEmployeeCode() {
        return "EMP-TMP-" + UUID.randomUUID();
    }

    private String formatEmployeeCode(Long employeeId) {
        return String.format("EMP-%04d", employeeId);
    }

    private String buildDefaultPassword(String employeeCode) {
        if (employeeCode == null || employeeCode.isBlank()) {
            return "Emp@0000";
        }

        String normalized = employeeCode.trim();
        int separatorIndex = normalized.lastIndexOf('-');
        String suffix = separatorIndex >= 0 && separatorIndex < normalized.length() - 1
                ? normalized.substring(separatorIndex + 1)
                : normalized;
        return "Emp@" + suffix;
    }

    // Chuyển trạng thái nhân viên sang INACTIVE thay vì xoá cứng.
    @Transactional
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        employee.setStatus("INACTIVE");
        Employee saved = employeeRepository.save(employee);
        syncAuthAccount(saved);
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
        ensureFaceEmbeddingIsUnique(employee.getId(), probe);
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

    private void ensureFaceEmbeddingIsUnique(Long employeeId, double[] probe) {
        List<Employee> candidates = employeeRepository.findByStatusAndFaceEmbeddingIsNotNull("ACTIVE");
        for (Employee candidate : candidates) {
            if (candidate.getId() == null || candidate.getId().equals(employeeId)) {
                continue;
            }
            try {
                double[] stored = FaceEmbeddingUtils.fromJson(candidate.getFaceEmbedding(), objectMapper);
                if (stored.length != FaceEmbeddingUtils.FACE_DESCRIPTOR_LENGTH) {
                    continue;
                }

                double distance = FaceEmbeddingUtils.euclideanDistance(probe, stored);
                if (distance <= faceMatchDistanceThreshold) {
                    throw new AppException(ErrorCode.INVALID_INPUT, "Khuôn mặt này đã được đăng ký trong hệ thống");
                }
            } catch (JsonProcessingException ex) {
                // Bỏ qua bản ghi không parse được để không chặn toàn bộ thao tác đăng ký.
            }
        }
    }

    private void syncAuthAccount(Employee employee) {
        if (employee.getUserId() == null) {
            return;
        }

        authClient.updateUser(employee.getUserId(), new InternalUpdateUserRequest(
                employee.getEmployeeCode(),
                employee.getEmail(),
                isActiveStatus(employee.getStatus())));
    }

    private boolean isActiveStatus(String status) {
        return status != null && "ACTIVE".equalsIgnoreCase(status.trim());
    }
}




