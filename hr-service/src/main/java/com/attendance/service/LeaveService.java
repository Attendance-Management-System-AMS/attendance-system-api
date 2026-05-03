package com.attendance.service;

import com.attendance.client.AttendanceClient;
import com.attendance.common.dto.AttendanceCorrectionSyncRequest;
import com.attendance.common.dto.LeaveApprovalSyncRequest;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.LeaveRequestRecord;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.dto.response.LeaveTypeResponse;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveType;
import com.attendance.exception.AppException;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.LeaveMapper;
import com.attendance.mapper.LeaveTypeMapper;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.LeaveRequestRepository;
import com.attendance.repository.LeaveTypeRepository;
import com.attendance.repository.spec.LeaveSpecifications;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;
    private final LeaveTypeMapper leaveTypeMapper;
    private final AttendanceClient attendanceClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public LeaveResponse createRequest(LeaveRequestRecord request) {
        if (request.employeeId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên là bắt buộc");
        }
        requireEmployee(request.employeeId());

        String leaveTypeCode = request.leaveTypeCode() == null ? null : request.leaveTypeCode().trim().toUpperCase();
        LeaveType leaveType = leaveTypeRepository.findByCode(leaveTypeCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Loại nghỉ không hợp lệ"));

        if (request.toDate().isBefore(request.fromDate())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày kết thúc phải sau ngày bắt đầu");
        }

        boolean overlapped = leaveRequestRepository
                .existsByEmployeeIdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
                        request.employeeId(),
                        List.of("PENDING", "APPROVED"),
                        request.toDate(),
                        request.fromDate());
        if (overlapped) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Khoảng ngày nghỉ bị trùng với đơn đang chờ duyệt hoặc đã duyệt");
        }

        // Validate đơn giải trình công (AC) phải có ít nhất 1 giờ bổ sung
        if ("AC".equals(leaveTypeCode)) {
            if (request.correctedCheckIn() == null && request.correctedCheckOut() == null) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Đơn giải trình công phải có ít nhất giờ vào hoặc giờ ra bổ sung");
            }
            // Đơn giải trình chỉ áp dụng 1 ngày
            if (!request.fromDate().equals(request.toDate())) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Đơn giải trình công chỉ áp dụng cho 1 ngày");
            }
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(request.employeeId());
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setFromDate(request.fromDate());
        leaveRequest.setToDate(request.toDate());
        leaveRequest.setReason(request.reason());
        leaveRequest.setCorrectedCheckIn(request.correctedCheckIn());
        leaveRequest.setCorrectedCheckOut(request.correctedCheckOut());

        long days = ChronoUnit.DAYS.between(request.fromDate(), request.toDate()) + 1;
        leaveRequest.setTotalDays((double) days);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> search(String keyword, Long employeeId, String status, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.findAll(LeaveSpecifications.matches(keyword, employeeId, status), pageable);
        Map<Long, HrEmployeeSnapshot> snapshots = loadEmployeeSnapshots(page.getContent());
        return PageResponse.of(page.map(leaveRequest -> toResponse(leaveRequest, snapshots)));
    }

    @Transactional(readOnly = true)
    public LeaveResponse getById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));
        return toResponse(leaveRequest);
    }

    @Transactional
    public LeaveResponse approve(Long id, Long approvedById) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể phê duyệt đơn đang chờ xử lý");
        }

        if (approvedById != null && approvedById.equals(leaveRequest.getEmployeeId())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không thể tự phê duyệt đơn nghỉ của chính mình");
        }

        leaveRequest.setStatus("APPROVED");
        if (approvedById != null) {
            requireEmployee(approvedById);
            leaveRequest.setApprovedById(approvedById);
        }

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // Phân biệt logic sync: nghỉ phép vs giải trình công
        if ("AC".equals(saved.getLeaveType().getCode())) {
            syncAttendanceCorrectionData(saved);
        } else {
            syncApprovedLeaveAttendance(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public LeaveResponse reject(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể từ chối đơn đang chờ xử lý");
        }

        leaveRequest.setStatus("REJECTED");
        return toResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public void delete(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_NOT_FOUND));

        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể huỷ đơn đang chờ xử lý");
        }

        leaveRequestRepository.delete(leaveRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepository.findByIsActive(true)
                .stream()
                .map(leaveTypeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasApprovedLeave(Long employeeId, LocalDate date) {
        return leaveRequestRepository.existsApprovedLeaveExcludingType(
                employeeId,
                "APPROVED",
                date,
                "AC");
    }

    private LeaveResponse toResponse(LeaveRequest leaveRequest) {
        HrEmployeeSnapshot employee = getEmployeeSnapshotOrNull(leaveRequest.getEmployeeId());
        HrEmployeeSnapshot approvedBy = leaveRequest.getApprovedById() == null
                ? null
                : getEmployeeSnapshotOrNull(leaveRequest.getApprovedById());
        return leaveMapper.toResponse(leaveRequest, employee, approvedBy);
    }

    private LeaveResponse toResponse(LeaveRequest leaveRequest, Map<Long, HrEmployeeSnapshot> snapshots) {
        HrEmployeeSnapshot employee = snapshots.get(leaveRequest.getEmployeeId());
        HrEmployeeSnapshot approvedBy = leaveRequest.getApprovedById() == null
                ? null
                : snapshots.get(leaveRequest.getApprovedById());
        return leaveMapper.toResponse(leaveRequest, employee, approvedBy);
    }

    private void requireEmployee(Long employeeId) {
        if (employeeId == null || !employeeRepository.existsById(employeeId)) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
    }

    private HrEmployeeSnapshot getEmployeeSnapshotOrNull(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeRepository.findById(employeeId)
                .map(this::toSnapshot)
                .orElse(null);
    }

    private Map<Long, HrEmployeeSnapshot> loadEmployeeSnapshots(List<LeaveRequest> leaveRequests) {
        List<Long> employeeIds = leaveRequests.stream()
                .flatMap(leaveRequest -> java.util.stream.Stream.of(leaveRequest.getEmployeeId(), leaveRequest.getApprovedById()))
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (employeeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, HrEmployeeSnapshot> snapshots = new LinkedHashMap<>();
        for (Employee employee : employeeRepository.findByIdIn(employeeIds)) {
            snapshots.put(employee.getId(), toSnapshot(employee));
        }
        return snapshots;
    }

    private HrEmployeeSnapshot toSnapshot(Employee employee) {
        return new HrEmployeeSnapshot(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null,
                employee.getPosition() != null ? employee.getPosition().getName() : null);
    }

    private void syncApprovedLeaveAttendance(LeaveRequest leaveRequest) {
        try {
            attendanceClient.syncApprovedLeave(new LeaveApprovalSyncRequest(
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getToDate()));
        } catch (FeignException ex) {
            String message = extractRemoteErrorMessage(ex, "Không thể đồng bộ bảng công sau khi duyệt đơn nghỉ");
            log.error(
                    "Đồng bộ bảng công thất bại khi duyệt đơn nghỉ id={}, employeeId={}, fromDate={}, toDate={}, status={}, message={}",
                    leaveRequest.getId(),
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getToDate(),
                    ex.status(),
                    message,
                    ex);
            throw new AppException(resolveAttendanceSyncErrorCode(ex), message);
        } catch (Exception ex) {
            log.error(
                    "Đồng bộ bảng công thất bại khi duyệt đơn nghỉ id={}, employeeId={}, fromDate={}, toDate={}",
                    leaveRequest.getId(),
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getToDate(),
                    ex);
            throw new AppException(
                    ErrorCode.UNCATEGORIZED_ERROR,
                    "Không thể đồng bộ bảng công sau khi duyệt đơn nghỉ");
        }
    }

    private void syncAttendanceCorrectionData(LeaveRequest leaveRequest) {
        try {
            attendanceClient.syncAttendanceCorrection(new AttendanceCorrectionSyncRequest(
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    leaveRequest.getCorrectedCheckIn(),
                    leaveRequest.getCorrectedCheckOut()));
        } catch (FeignException ex) {
            String message = extractRemoteErrorMessage(ex, "Không thể đồng bộ bảng công sau khi duyệt đơn giải trình");
            log.error(
                    "Đồng bộ giải trình công thất bại id={}, employeeId={}, workDate={}, status={}, message={}",
                    leaveRequest.getId(),
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    ex.status(),
                    message,
                    ex);
            throw new AppException(resolveAttendanceSyncErrorCode(ex), message);
        } catch (Exception ex) {
            log.error(
                    "Đồng bộ giải trình công thất bại id={}, employeeId={}, workDate={}",
                    leaveRequest.getId(),
                    leaveRequest.getEmployeeId(),
                    leaveRequest.getFromDate(),
                    ex);
            throw new AppException(
                    ErrorCode.UNCATEGORIZED_ERROR,
                    "Không thể đồng bộ bảng công sau khi duyệt đơn giải trình");
        }
    }

    private ErrorCode resolveAttendanceSyncErrorCode(FeignException ex) {
        return switch (ex.status()) {
            case 400 -> ErrorCode.INVALID_INPUT;
            case 401 -> ErrorCode.UNAUTHORIZED;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            default -> ErrorCode.UNCATEGORIZED_ERROR;
        };
    }

    private String extractRemoteErrorMessage(FeignException ex, String fallbackMessage) {
        String body = ex.contentUTF8();
        if (body == null || body.isBlank()) {
            return fallbackMessage;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode messageNode = root.path("message");
            if (messageNode.isTextual() && !messageNode.asText().isBlank()) {
                return messageNode.asText();
            }
            JsonNode dataMessageNode = root.path("data").path("message");
            if (dataMessageNode.isTextual() && !dataMessageNode.asText().isBlank()) {
                return dataMessageNode.asText();
            }
        } catch (Exception ignored) {
            // Fallback to a generic message below when the upstream body is not JSON.
        }

        return fallbackMessage;
    }
}
