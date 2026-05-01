package com.attendance.service;

import com.attendance.client.HrClient;
import com.attendance.common.dto.PageResponse;
import com.attendance.dto.request.OvertimeApprovalRequest;
import com.attendance.dto.request.OvertimeRequestRecord;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.OvertimeRequestResponse;
import com.attendance.entity.Attendance;
import com.attendance.entity.EmployeeSchedule;
import com.attendance.entity.OvertimeRequest;
import com.attendance.entity.Shift;
import com.attendance.exception.AppException;
import com.attendance.exception.ErrorCode;
import com.attendance.mapper.OvertimeRequestMapper;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeScheduleRepository;
import com.attendance.repository.HolidayRepository;
import com.attendance.repository.OvertimeRequestRepository;
import com.attendance.repository.spec.OvertimeRequestSpecifications;
import com.attendance.util.ShiftUtils;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OvertimeRequestService {

    private static final List<String> ACTIVE_REQUEST_STATUSES = List.of("PENDING", "APPROVED");
    private static final List<String> RECALC_REQUEST_STATUSES = List.of("PENDING", "APPROVED", "REJECTED");

    private final OvertimeRequestRepository overtimeRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final HolidayRepository holidayRepository;
    private final HrClient hrClient;
    private final OvertimeRequestMapper overtimeRequestMapper;

    @Value("${app.overtime.minimum-minutes:30}")
    private int minimumOvertimeMinutes;

    @Value("${app.overtime.rounding-block-minutes:30}")
    private int overtimeRoundingBlockMinutes;

    @Transactional
    public OvertimeRequestResponse createForEmployee(OvertimeRequestRecord request) {
        if (request.employeeId() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Mã nhân viên là bắt buộc");
        }
        return createInternal(request.employeeId(), request);
    }

    @Transactional
    public OvertimeRequestResponse createForCurrentEmployee(Long employeeId, OvertimeRequestRecord request) {
        return createInternal(employeeId, request);
    }

    @Transactional(readOnly = true)
    public PageResponse<OvertimeRequestResponse> search(
            String keyword,
            Long employeeId,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable) {
        Page<OvertimeRequest> page = overtimeRequestRepository.findAll(
                OvertimeRequestSpecifications.matches(keyword, employeeId, status, fromDate, toDate),
                pageable);
        Map<Long, HrEmployeeSnapshot> snapshots = loadEmployeeSnapshots(page.getContent());
        Map<String, Attendance> attendanceByRequest = loadAttendanceMap(page.getContent());
        return PageResponse.of(page.map(request -> toResponse(request, snapshots, attendanceByRequest)));
    }

    @Transactional(readOnly = true)
    public OvertimeRequestResponse getById(Long id) {
        OvertimeRequest request = requireRequest(id);
        return toResponse(request);
    }

    @Transactional
    public OvertimeRequestResponse approve(Long id, Long approvedById, OvertimeApprovalRequest approval) {
        OvertimeRequest request = requireRequest(id);
        requirePending(request);

        if (approvedById != null && approvedById.equals(request.getEmployeeId())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không thể tự phê duyệt đơn tăng ca của chính mình");
        }
        if (approvedById != null) {
            requireEmployee(approvedById);
        }

        request.setStatus("APPROVED");
        request.setApprovedById(approvedById);
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovalNote(normalizeNote(approval == null ? null : approval.note()));
        OvertimeRequest saved = overtimeRequestRepository.save(request);
        recalculateAttendanceOvertime(saved.getEmployeeId(), saved.getWorkDate());
        return toResponse(saved);
    }

    @Transactional
    public OvertimeRequestResponse reject(Long id, Long approvedById, OvertimeApprovalRequest approval) {
        OvertimeRequest request = requireRequest(id);
        requirePending(request);

        if (approvedById != null && approvedById.equals(request.getEmployeeId())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không thể tự từ chối đơn tăng ca của chính mình");
        }
        if (approvedById != null) {
            requireEmployee(approvedById);
        }

        request.setStatus("REJECTED");
        request.setApprovedById(approvedById);
        request.setApprovedAt(LocalDateTime.now());
        request.setApprovalNote(normalizeNote(approval == null ? null : approval.note()));
        OvertimeRequest saved = overtimeRequestRepository.save(request);
        recalculateAttendanceOvertime(saved.getEmployeeId(), saved.getWorkDate());
        return toResponse(saved);
    }

    @Transactional
    public OvertimeRequestResponse cancel(Long id, Long employeeId) {
        OvertimeRequest request = requireRequest(id);
        if (!Objects.equals(request.getEmployeeId(), employeeId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền huỷ đơn tăng ca này");
        }
        requirePending(request);
        request.setStatus("CANCELLED");
        OvertimeRequest saved = overtimeRequestRepository.save(request);
        recalculateAttendanceOvertime(saved.getEmployeeId(), saved.getWorkDate());
        return toResponse(saved);
    }

    @Transactional
    public OvertimeRequestResponse cancelByManager(Long id) {
        OvertimeRequest request = requireRequest(id);
        requirePending(request);
        request.setStatus("CANCELLED");
        OvertimeRequest saved = overtimeRequestRepository.save(request);
        recalculateAttendanceOvertime(saved.getEmployeeId(), saved.getWorkDate());
        return toResponse(saved);
    }

    @Transactional
    public void recalculateAttendanceOvertime(Long employeeId, LocalDate workDate) {
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, workDate).orElse(null);
        if (attendance == null) {
            return;
        }

        OvertimeCalculation calculation = calculateOvertime(attendance);
        attendance.setActualOvertimeMinutes(calculation.actualMinutes());
        attendance.setApprovedOvertimeMinutes(calculation.approvedMinutes());
        attendance.setPayableOvertimeMinutes(calculation.payableMinutes());
        attendance.setOvertimeStatus(calculation.status());
        attendanceRepository.save(attendance);
    }

    private OvertimeRequestResponse createInternal(Long employeeId, OvertimeRequestRecord request) {
        requireEmployee(employeeId);
        validateRequestWindow(request.workDate(), request.startTime(), request.endTime());

        List<OvertimeRequest> activeRequests = overtimeRequestRepository.findByEmployeeIdAndWorkDateAndStatusIn(
                employeeId,
                request.workDate(),
                ACTIVE_REQUEST_STATUSES);
        if (activeRequests.stream().anyMatch(existing -> overlaps(existing, request.startTime(), request.endTime()))) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Khoảng thời gian tăng ca bị trùng với đơn đang chờ duyệt hoặc đã duyệt");
        }

        OvertimeRequest overtimeRequest = new OvertimeRequest();
        overtimeRequest.setEmployeeId(employeeId);
        overtimeRequest.setWorkDate(request.workDate());
        overtimeRequest.setStartTime(request.startTime());
        overtimeRequest.setEndTime(request.endTime());
        overtimeRequest.setRequestedMinutes(resolveMinutes(request.workDate(), request.startTime(), request.endTime()));
        overtimeRequest.setReason(request.reason() == null ? null : request.reason().trim());
        overtimeRequest.setStatus("PENDING");

        OvertimeRequest saved = overtimeRequestRepository.save(overtimeRequest);
        recalculateAttendanceOvertime(saved.getEmployeeId(), saved.getWorkDate());
        return toResponse(saved);
    }

    private OvertimeCalculation calculateOvertime(Attendance attendance) {
        List<OvertimeRequest> requests = overtimeRequestRepository.findByEmployeeIdAndWorkDateAndStatusIn(
                attendance.getEmployeeId(),
                attendance.getWorkDate(),
                RECALC_REQUEST_STATUSES);
        int approvedMinutes = requests.stream()
                .filter(request -> "APPROVED".equalsIgnoreCase(request.getStatus()))
                .mapToInt(OvertimeRequest::getRequestedMinutes)
                .sum();

        if (attendance.getCheckInTime() == null || attendance.getCheckOutTime() == null) {
            return new OvertimeCalculation(0, approvedMinutes, 0, requests.isEmpty() ? "NONE" : "NO_CHECKOUT");
        }

        List<TimeWindow> eligibleWindows = resolveEligibleOvertimeWindows(attendance);
        int actualMinutes = sumWindows(eligibleWindows);
        int payableMinutes = requests.stream()
                .filter(request -> "APPROVED".equalsIgnoreCase(request.getStatus()))
                .map(this::toWindow)
                .mapToInt(requestWindow -> sumIntersections(requestWindow, eligibleWindows))
                .sum();

        payableMinutes = applyRounding(Math.min(payableMinutes, actualMinutes));
        String status = resolveAttendanceOvertimeStatus(actualMinutes, payableMinutes, requests);
        return new OvertimeCalculation(actualMinutes, approvedMinutes, payableMinutes, status);
    }

    private List<TimeWindow> resolveEligibleOvertimeWindows(Attendance attendance) {
        LocalDateTime checkIn = attendance.getCheckInTime();
        LocalDateTime checkOut = attendance.getCheckOutTime();
        TimeWindow workedWindow = new TimeWindow(checkIn, checkOut);
        boolean holiday = holidayRepository.existsByFromDateLessThanEqualAndToDateGreaterThanEqual(
                attendance.getWorkDate(),
                attendance.getWorkDate());
        Shift shift = resolveShift(attendance.getEmployeeId(), attendance.getWorkDate());

        if (holiday || shift == null) {
            return List.of(workedWindow);
        }

        LocalDateTime scheduledStart = attendance.getWorkDate().atTime(shift.getStartTime());
        LocalDateTime scheduledEnd = ShiftUtils.resolveShiftEnd(attendance.getWorkDate(), shift);
        TimeWindow beforeShift = new TimeWindow(checkIn, min(checkOut, scheduledStart));
        TimeWindow afterShift = new TimeWindow(max(checkIn, scheduledEnd), checkOut);

        return List.of(beforeShift, afterShift)
                .stream()
                .filter(TimeWindow::hasPositiveDuration)
                .toList();
    }

    private String resolveAttendanceOvertimeStatus(
            int actualMinutes,
            int payableMinutes,
            List<OvertimeRequest> requests) {
        if (payableMinutes > 0) {
            return "APPROVED";
        }
        if (requests.stream().anyMatch(request -> "PENDING".equalsIgnoreCase(request.getStatus()))) {
            return "PENDING_APPROVAL";
        }
        if (requests.stream().anyMatch(request -> "REJECTED".equalsIgnoreCase(request.getStatus()))) {
            return "REJECTED";
        }
        if (actualMinutes > 0) {
            return "UNAPPROVED";
        }
        return "NONE";
    }

    private int applyRounding(int minutes) {
        if (minutes < minimumOvertimeMinutes) {
            return 0;
        }
        if (overtimeRoundingBlockMinutes <= 0) {
            return minutes;
        }
        return minutes / overtimeRoundingBlockMinutes * overtimeRoundingBlockMinutes;
    }

    private int sumWindows(List<TimeWindow> windows) {
        return windows.stream().mapToInt(TimeWindow::minutes).sum();
    }

    private int sumIntersections(TimeWindow requestWindow, List<TimeWindow> eligibleWindows) {
        return eligibleWindows.stream()
                .map(requestWindow::intersect)
                .filter(TimeWindow::hasPositiveDuration)
                .mapToInt(TimeWindow::minutes)
                .sum();
    }

    private TimeWindow toWindow(OvertimeRequest request) {
        LocalDateTime start = request.getWorkDate().atTime(request.getStartTime());
        LocalDateTime end = request.getWorkDate().atTime(request.getEndTime());
        if (!end.isAfter(start)) {
            end = end.plusDays(1);
        }
        return new TimeWindow(start, end);
    }

    private boolean overlaps(OvertimeRequest existing, LocalTime startTime, LocalTime endTime) {
        TimeWindow existingWindow = toWindow(existing);
        TimeWindow newWindow = new TimeWindow(
                existing.getWorkDate().atTime(startTime),
                resolveEndDateTime(existing.getWorkDate(), startTime, endTime));
        return existingWindow.intersect(newWindow).hasPositiveDuration();
    }

    private void validateRequestWindow(LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        if (workDate == null || startTime == null || endTime == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Ngày và giờ tăng ca là bắt buộc");
        }
        int minutes = resolveMinutes(workDate, startTime, endTime);
        if (minutes <= 0) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Giờ kết thúc tăng ca phải sau giờ bắt đầu");
        }
        if (minutes > 16 * 60) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Thời lượng tăng ca không được vượt quá 16 giờ");
        }
    }

    private int resolveMinutes(LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        return Math.toIntExact(Duration.between(
                workDate.atTime(startTime),
                resolveEndDateTime(workDate, startTime, endTime)).toMinutes());
    }

    private LocalDateTime resolveEndDateTime(LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        LocalDateTime start = workDate.atTime(startTime);
        LocalDateTime end = workDate.atTime(endTime);
        if (!end.isAfter(start)) {
            end = end.plusDays(1);
        }
        return end;
    }

    private Shift resolveShift(Long employeeId, LocalDate workDate) {
        int dayOfWeek = workDate.getDayOfWeek().getValue();
        EmployeeSchedule selected = null;
        for (EmployeeSchedule schedule : employeeScheduleRepository
                .findByEmployeeIdAndIsActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(employeeId, workDate)) {
            if (schedule.getEffectiveTo() != null && schedule.getEffectiveTo().isBefore(workDate)) {
                continue;
            }
            if (!Objects.equals(schedule.getDayOfWeek(), dayOfWeek)) {
                continue;
            }
            selected = schedule;
            break;
        }
        return selected == null ? null : selected.getShift();
    }

    private OvertimeRequest requireRequest(Long id) {
        return overtimeRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy đơn tăng ca"));
    }

    private void requirePending(OvertimeRequest request) {
        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể xử lý đơn tăng ca đang chờ duyệt");
        }
    }

    private void requireEmployee(Long employeeId) {
        if (employeeId == null || !hrClient.employeeExists(employeeId)) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
    }

    private OvertimeRequestResponse toResponse(OvertimeRequest request) {
        HrEmployeeSnapshot employee = getEmployeeSnapshotOrNull(request.getEmployeeId());
        HrEmployeeSnapshot approvedBy = getEmployeeSnapshotOrNull(request.getApprovedById());
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(
                request.getEmployeeId(),
                request.getWorkDate()).orElse(null);
        return overtimeRequestMapper.toResponse(request, employee, approvedBy, attendance);
    }

    private OvertimeRequestResponse toResponse(
            OvertimeRequest request,
            Map<Long, HrEmployeeSnapshot> snapshots,
            Map<String, Attendance> attendanceByRequest) {
        return overtimeRequestMapper.toResponse(
                request,
                snapshots.get(request.getEmployeeId()),
                snapshots.get(request.getApprovedById()),
                attendanceByRequest.get(attendanceKey(request.getEmployeeId(), request.getWorkDate())));
    }

    private Map<String, Attendance> loadAttendanceMap(List<OvertimeRequest> requests) {
        if (requests.isEmpty()) {
            return Map.of();
        }

        List<Long> employeeIds = requests.stream()
                .map(OvertimeRequest::getEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (employeeIds.isEmpty()) {
            return Map.of();
        }

        LocalDate fromDate = null;
        LocalDate toDate = null;
        for (OvertimeRequest request : requests) {
            if (request.getWorkDate() == null) {
                continue;
            }
            if (fromDate == null || request.getWorkDate().isBefore(fromDate)) {
                fromDate = request.getWorkDate();
            }
            if (toDate == null || request.getWorkDate().isAfter(toDate)) {
                toDate = request.getWorkDate();
            }
        }
        if (fromDate == null || toDate == null) {
            return Map.of();
        }

        Map<String, Attendance> attendances = new LinkedHashMap<>();
        for (Attendance attendance : attendanceRepository.findByEmployeeIdInAndWorkDateBetween(employeeIds, fromDate, toDate)) {
            attendances.put(attendanceKey(attendance.getEmployeeId(), attendance.getWorkDate()), attendance);
        }
        return attendances;
    }

    private String attendanceKey(Long employeeId, LocalDate workDate) {
        return employeeId + "|" + workDate;
    }

    private HrEmployeeSnapshot getEmployeeSnapshotOrNull(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        try {
            return hrClient.getEmployeeSnapshot(employeeId);
        } catch (Exception ex) {
            log.warn("Không lấy được snapshot nhân viên employeeId={}: {}", employeeId, ex.getMessage());
            return null;
        }
    }

    private Map<Long, HrEmployeeSnapshot> loadEmployeeSnapshots(List<OvertimeRequest> requests) {
        List<Long> employeeIds = requests.stream()
                .flatMap(request -> java.util.stream.Stream.of(request.getEmployeeId(), request.getApprovedById()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (employeeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, HrEmployeeSnapshot> snapshots = new LinkedHashMap<>();
        try {
            for (HrEmployeeSnapshot snapshot : hrClient.findEmployeeSnapshotsByIds(employeeIds)) {
                if (snapshot != null && snapshot.id() != null) {
                    snapshots.put(snapshot.id(), snapshot);
                }
            }
        } catch (Exception ex) {
            log.warn("Không lấy được batch snapshot nhân viên khi tải đơn tăng ca: {}", ex.getMessage());
        }
        return snapshots;
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private record TimeWindow(LocalDateTime start, LocalDateTime end) {
        private boolean hasPositiveDuration() {
            return start != null && end != null && end.isAfter(start);
        }

        private int minutes() {
            if (!hasPositiveDuration()) {
                return 0;
            }
            return Math.toIntExact(Duration.between(start, end).toMinutes());
        }

        private TimeWindow intersect(TimeWindow other) {
            LocalDateTime intersectionStart = start.isAfter(other.start) ? start : other.start;
            LocalDateTime intersectionEnd = end.isBefore(other.end) ? end : other.end;
            return new TimeWindow(intersectionStart, intersectionEnd);
        }
    }

    private record OvertimeCalculation(
            int actualMinutes,
            int approvedMinutes,
            int payableMinutes,
            String status) {
    }
}
