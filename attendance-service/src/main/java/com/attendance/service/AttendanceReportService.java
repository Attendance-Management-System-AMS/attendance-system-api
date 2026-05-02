package com.attendance.service;

import com.attendance.client.HrClient;
import com.attendance.dto.response.AttendanceAnnualSummaryResponse;
import com.attendance.dto.response.AttendanceMonthlySummaryItemResponse;
import com.attendance.dto.response.EmployeeAttendanceSummaryResponse;
import com.attendance.dto.response.EmployeeOvertimeSummaryResponse;
import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.OvertimeMonthlySummaryItemResponse;
import com.attendance.dto.response.OvertimeSummaryResponse;
import com.attendance.entity.Attendance;
import com.attendance.entity.OvertimeRequest;
import com.attendance.exception.AppException;
import com.attendance.exception.ErrorCode;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.OvertimeRequestRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceReportService {

    private final AttendanceRepository attendanceRepository;
    private final OvertimeRequestRepository overtimeRequestRepository;
    private final HrClient hrClient;
    private final Clock clock;

    public AttendanceAnnualSummaryResponse getAnnualAttendanceSummary(int year, Long departmentId, Long employeeId) {
        LocalDate fromDate = Year.of(year).atDay(1);
        LocalDate toDate = fromDate.withMonth(12).withDayOfMonth(31);

        List<Attendance> rawAttendances = loadAttendances(employeeId, fromDate, toDate);
        ReportScope scope = resolveAttendanceScope(rawAttendances, departmentId, employeeId);
        List<Attendance> attendances = filterAttendances(rawAttendances, scope.employeeIds());
        Map<Long, List<Attendance>> attendancesByEmployee = groupAttendancesByEmployee(attendances);

        List<EmployeeAttendanceSummaryResponse> employees = scope.employees().stream()
                .map(snapshot -> buildEmployeeAttendanceSummary(snapshot, attendancesByEmployee.get(snapshot.id()), year))
                .sorted(Comparator
                        .comparingInt(EmployeeAttendanceSummaryResponse::workedMinutes)
                        .reversed()
                        .thenComparing(EmployeeAttendanceSummaryResponse::fullName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        List<AttendanceMonthlySummaryItemResponse> months = buildAttendanceMonthlySummaries(attendances, year);
        AttendanceAccumulator totals = new AttendanceAccumulator();
        employees.forEach(totals::addEmployeeSummary);

        return new AttendanceAnnualSummaryResponse(
                year,
                fromDate,
                toDate,
                departmentId,
                employeeId,
                employees.size(),
                totals.workDays,
                totals.lateDays,
                totals.earlyLeaveDays,
                totals.absentDays,
                totals.leaveDays,
                totals.holidayDays,
                totals.missingCheckoutDays,
                totals.incompleteDays,
                totals.workedMinutes,
                totals.overtimeMinutes,
                months,
                employees);
    }

    public OvertimeSummaryResponse getOvertimeSummary(int year, Long departmentId, Long employeeId) {
        LocalDate fromDate = Year.of(year).atDay(1);
        LocalDate toDate = fromDate.withMonth(12).withDayOfMonth(31);

        List<OvertimeRequest> rawRequests = loadOvertimeRequests(employeeId, fromDate, toDate);
        List<Attendance> rawAttendances = loadAttendances(employeeId, fromDate, toDate);
        ReportScope scope = resolveOvertimeScope(rawRequests, rawAttendances, departmentId, employeeId);

        List<OvertimeRequest> requests = filterRequests(rawRequests, scope.employeeIds());
        List<Attendance> attendances = filterAttendances(rawAttendances, scope.employeeIds());
        Map<Long, List<OvertimeRequest>> requestsByEmployee = groupRequestsByEmployee(requests);
        Map<Long, List<Attendance>> attendancesByEmployee = groupAttendancesByEmployee(attendances);

        List<EmployeeOvertimeSummaryResponse> employees = scope.employees().stream()
                .map(snapshot -> buildEmployeeOvertimeSummary(
                        snapshot,
                        requestsByEmployee.get(snapshot.id()),
                        attendancesByEmployee.get(snapshot.id())))
                .filter(summary ->
                        summary.requestCount() > 0
                                || summary.actualMinutes() > 0
                                || summary.payableMinutes() > 0
                                || employeeId != null)
                .sorted(Comparator
                        .comparingInt(EmployeeOvertimeSummaryResponse::payableMinutes)
                        .reversed()
                        .thenComparing(Comparator.comparingInt(EmployeeOvertimeSummaryResponse::requestedMinutes).reversed())
                        .thenComparing(EmployeeOvertimeSummaryResponse::fullName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        List<OvertimeMonthlySummaryItemResponse> months = buildOvertimeMonthlySummaries(requests, attendances, year);
        OvertimeAccumulator totals = new OvertimeAccumulator();
        employees.forEach(totals::addEmployeeSummary);

        return new OvertimeSummaryResponse(
                year,
                fromDate,
                toDate,
                departmentId,
                employeeId,
                employees.size(),
                totals.requestCount,
                totals.pendingRequests,
                totals.approvedRequests,
                totals.rejectedRequests,
                totals.cancelledRequests,
                totals.requestedMinutes,
                totals.approvedMinutes,
                totals.actualMinutes,
                totals.payableMinutes,
                months,
                employees);
    }

    private ReportScope resolveAttendanceScope(List<Attendance> attendances, Long departmentId, Long employeeId) {
        if (employeeId != null) {
            HrEmployeeSnapshot snapshot = requireEmployeeSnapshot(employeeId);
            return new ReportScope(List.of(snapshot), Set.of(employeeId));
        }

        if (departmentId != null) {
            List<HrEmployeeSnapshot> employees = hrClient.findEmployeeSnapshots(departmentId, null, null);
            return new ReportScope(employees, toEmployeeIdSet(employees));
        }

        Map<Long, HrEmployeeSnapshot> snapshots = new LinkedHashMap<>();
        for (HrEmployeeSnapshot snapshot : hrClient.findEmployeeSnapshots(null, null, "ACTIVE")) {
            addSnapshot(snapshots, snapshot);
        }
        mergeSnapshotsByIds(snapshots, attendances.stream().map(Attendance::getEmployeeId).toList());
        return new ReportScope(new ArrayList<>(snapshots.values()), snapshots.keySet());
    }

    private ReportScope resolveOvertimeScope(
            List<OvertimeRequest> requests,
            List<Attendance> attendances,
            Long departmentId,
            Long employeeId) {
        if (employeeId != null) {
            HrEmployeeSnapshot snapshot = requireEmployeeSnapshot(employeeId);
            return new ReportScope(List.of(snapshot), Set.of(employeeId));
        }

        if (departmentId != null) {
            List<HrEmployeeSnapshot> employees = hrClient.findEmployeeSnapshots(departmentId, null, null);
            return new ReportScope(employees, toEmployeeIdSet(employees));
        }

        LinkedHashSet<Long> employeeIds = new LinkedHashSet<>();
        requests.stream().map(OvertimeRequest::getEmployeeId).filter(Objects::nonNull).forEach(employeeIds::add);
        attendances.stream()
                .filter(this::hasOvertimeSignal)
                .map(Attendance::getEmployeeId)
                .filter(Objects::nonNull)
                .forEach(employeeIds::add);

        Map<Long, HrEmployeeSnapshot> snapshots = new LinkedHashMap<>();
        mergeSnapshotsByIds(snapshots, employeeIds);
        return new ReportScope(new ArrayList<>(snapshots.values()), snapshots.keySet());
    }

    private HrEmployeeSnapshot requireEmployeeSnapshot(Long employeeId) {
        HrEmployeeSnapshot snapshot = hrClient.getEmployeeSnapshot(employeeId);
        if (snapshot == null) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, "Không tìm thấy nhân viên");
        }
        return snapshot;
    }

    private List<Attendance> loadAttendances(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        if (employeeId != null) {
            return attendanceRepository.findByEmployeeIdInAndWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
                    List.of(employeeId),
                    fromDate,
                    toDate);
        }
        return attendanceRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(fromDate, toDate);
    }

    private List<OvertimeRequest> loadOvertimeRequests(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        if (employeeId != null) {
            return overtimeRequestRepository.findByEmployeeIdInAndWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(
                    List.of(employeeId),
                    fromDate,
                    toDate);
        }
        return overtimeRequestRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(fromDate, toDate);
    }

    private void mergeSnapshotsByIds(Map<Long, HrEmployeeSnapshot> snapshots, Collection<Long> employeeIds) {
        List<Long> ids = employeeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(id -> !snapshots.containsKey(id))
                .toList();
        if (ids.isEmpty()) {
            return;
        }

        for (HrEmployeeSnapshot snapshot : hrClient.findEmployeeSnapshotsByIds(ids)) {
            addSnapshot(snapshots, snapshot);
        }
    }

    private void addSnapshot(Map<Long, HrEmployeeSnapshot> snapshots, HrEmployeeSnapshot snapshot) {
        if (snapshot != null && snapshot.id() != null) {
            snapshots.putIfAbsent(snapshot.id(), snapshot);
        }
    }

    private Set<Long> toEmployeeIdSet(List<HrEmployeeSnapshot> employees) {
        return employees.stream()
                .map(HrEmployeeSnapshot::id)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    private List<Attendance> filterAttendances(List<Attendance> attendances, Set<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return List.of();
        }
        return attendances.stream()
                .filter(attendance -> employeeIds.contains(attendance.getEmployeeId()))
                .toList();
    }

    private List<OvertimeRequest> filterRequests(List<OvertimeRequest> requests, Set<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return List.of();
        }
        return requests.stream()
                .filter(request -> employeeIds.contains(request.getEmployeeId()))
                .toList();
    }

    private Map<Long, List<Attendance>> groupAttendancesByEmployee(List<Attendance> attendances) {
        Map<Long, List<Attendance>> result = new LinkedHashMap<>();
        for (Attendance attendance : attendances) {
            if (attendance.getEmployeeId() == null) {
                continue;
            }
            result.computeIfAbsent(attendance.getEmployeeId(), ignored -> new ArrayList<>()).add(attendance);
        }
        return result;
    }

    private Map<Long, List<OvertimeRequest>> groupRequestsByEmployee(List<OvertimeRequest> requests) {
        Map<Long, List<OvertimeRequest>> result = new LinkedHashMap<>();
        for (OvertimeRequest request : requests) {
            if (request.getEmployeeId() == null) {
                continue;
            }
            result.computeIfAbsent(request.getEmployeeId(), ignored -> new ArrayList<>()).add(request);
        }
        return result;
    }

    private List<AttendanceMonthlySummaryItemResponse> buildAttendanceMonthlySummaries(List<Attendance> attendances, int year) {
        List<AttendanceMonthlySummaryItemResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            AttendanceAccumulator accumulator = new AttendanceAccumulator();
            for (Attendance attendance : attendances) {
                if (attendance.getWorkDate() == null
                        || attendance.getWorkDate().getYear() != year
                        || attendance.getWorkDate().getMonthValue() != month) {
                    continue;
                }
                accumulator.addAttendance(attendance, normalizeAttendanceStatus(attendance));
            }
            result.add(new AttendanceMonthlySummaryItemResponse(
                    month,
                    monthLabel(month),
                    accumulator.workDays,
                    accumulator.lateDays,
                    accumulator.earlyLeaveDays,
                    accumulator.absentDays,
                    accumulator.leaveDays,
                    accumulator.holidayDays,
                    accumulator.missingCheckoutDays,
                    accumulator.incompleteDays,
                    accumulator.workedMinutes,
                    accumulator.overtimeMinutes));
        }
        return result;
    }

    private List<OvertimeMonthlySummaryItemResponse> buildOvertimeMonthlySummaries(
            List<OvertimeRequest> requests,
            List<Attendance> attendances,
            int year) {
        List<OvertimeMonthlySummaryItemResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            OvertimeAccumulator accumulator = new OvertimeAccumulator();
            for (OvertimeRequest request : requests) {
                if (request.getWorkDate() == null
                        || request.getWorkDate().getYear() != year
                        || request.getWorkDate().getMonthValue() != month) {
                    continue;
                }
                accumulator.addRequest(request);
            }
            for (Attendance attendance : attendances) {
                if (attendance.getWorkDate() == null
                        || attendance.getWorkDate().getYear() != year
                        || attendance.getWorkDate().getMonthValue() != month) {
                    continue;
                }
                accumulator.addAttendance(attendance);
            }
            result.add(new OvertimeMonthlySummaryItemResponse(
                    month,
                    monthLabel(month),
                    accumulator.requestCount,
                    accumulator.pendingRequests,
                    accumulator.approvedRequests,
                    accumulator.rejectedRequests,
                    accumulator.cancelledRequests,
                    accumulator.requestedMinutes,
                    accumulator.approvedMinutes,
                    accumulator.actualMinutes,
                    accumulator.payableMinutes));
        }
        return result;
    }

    private EmployeeAttendanceSummaryResponse buildEmployeeAttendanceSummary(
            HrEmployeeSnapshot snapshot,
            List<Attendance> attendances,
            int year) {
        AttendanceAccumulator accumulator = new AttendanceAccumulator();
        if (attendances != null) {
            for (Attendance attendance : attendances) {
                accumulator.addAttendance(attendance, normalizeAttendanceStatus(attendance));
            }
        }
        List<AttendanceMonthlySummaryItemResponse> months = buildAttendanceMonthlySummaries(
                attendances == null ? List.of() : attendances,
                year);
        return new EmployeeAttendanceSummaryResponse(
                snapshot.id(),
                snapshot.employeeCode(),
                snapshot.fullName(),
                snapshot.departmentName(),
                snapshot.positionName(),
                accumulator.workDays,
                accumulator.lateDays,
                accumulator.earlyLeaveDays,
                accumulator.absentDays,
                accumulator.leaveDays,
                accumulator.holidayDays,
                accumulator.missingCheckoutDays,
                accumulator.incompleteDays,
                accumulator.workedMinutes,
                accumulator.overtimeMinutes,
                months);
    }

    private EmployeeOvertimeSummaryResponse buildEmployeeOvertimeSummary(
            HrEmployeeSnapshot snapshot,
            List<OvertimeRequest> requests,
            List<Attendance> attendances) {
        OvertimeAccumulator accumulator = new OvertimeAccumulator();
        if (requests != null) {
            requests.forEach(accumulator::addRequest);
        }
        if (attendances != null) {
            attendances.forEach(accumulator::addAttendance);
        }
        return new EmployeeOvertimeSummaryResponse(
                snapshot.id(),
                snapshot.employeeCode(),
                snapshot.fullName(),
                snapshot.departmentName(),
                snapshot.positionName(),
                accumulator.requestCount,
                accumulator.pendingRequests,
                accumulator.approvedRequests,
                accumulator.rejectedRequests,
                accumulator.cancelledRequests,
                accumulator.requestedMinutes,
                accumulator.approvedMinutes,
                accumulator.actualMinutes,
                accumulator.payableMinutes);
    }

    private boolean hasOvertimeSignal(Attendance attendance) {
        return valueOrZero(attendance.getActualOvertimeMinutes()) > 0
                || valueOrZero(attendance.getApprovedOvertimeMinutes()) > 0
                || valueOrZero(attendance.getPayableOvertimeMinutes()) > 0;
    }

    private String normalizeAttendanceStatus(Attendance attendance) {
        if (attendance == null) {
            return "";
        }

        if (attendance.getWorkDate() != null
                && attendance.getWorkDate().isBefore(LocalDate.now(clock))
                && attendance.getCheckInTime() != null
                && attendance.getCheckOutTime() == null) {
            return "MISSING_CHECKOUT";
        }

        return attendance.getStatus() == null ? "" : attendance.getStatus().trim().toUpperCase(Locale.ROOT);
    }

    private String monthLabel(int month) {
        return "Tháng " + String.format("%02d", month);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private record ReportScope(List<HrEmployeeSnapshot> employees, Set<Long> employeeIds) {
    }

    private final class AttendanceAccumulator {
        private int workDays;
        private int lateDays;
        private int earlyLeaveDays;
        private int absentDays;
        private int leaveDays;
        private int holidayDays;
        private int missingCheckoutDays;
        private int incompleteDays;
        private int workedMinutes;
        private int overtimeMinutes;

        private void addAttendance(Attendance attendance, String normalizedStatus) {
            if (attendance.getCheckInTime() != null) {
                workDays++;
            }
            if ("LATE".equals(normalizedStatus) || "LATE_AND_EARLY_LEAVE".equals(normalizedStatus)) {
                lateDays++;
            }
            if ("EARLY_LEAVE".equals(normalizedStatus) || "LATE_AND_EARLY_LEAVE".equals(normalizedStatus)) {
                earlyLeaveDays++;
            }
            switch (normalizedStatus) {
                case "ABSENT" -> absentDays++;
                case "ON_LEAVE", "LEAVE" -> leaveDays++;
                case "HOLIDAY" -> holidayDays++;
                case "MISSING_CHECKOUT" -> missingCheckoutDays++;
                case "INCOMPLETE" -> incompleteDays++;
                default -> {
                }
            }
            workedMinutes += valueOrZero(attendance.getWorkedMinutes());
            overtimeMinutes += valueOrZero(attendance.getPayableOvertimeMinutes());
        }

        private void addEmployeeSummary(EmployeeAttendanceSummaryResponse summary) {
            workDays += summary.workDays();
            lateDays += summary.lateDays();
            earlyLeaveDays += summary.earlyLeaveDays();
            absentDays += summary.absentDays();
            leaveDays += summary.leaveDays();
            holidayDays += summary.holidayDays();
            missingCheckoutDays += summary.missingCheckoutDays();
            incompleteDays += summary.incompleteDays();
            workedMinutes += summary.workedMinutes();
            overtimeMinutes += summary.overtimeMinutes();
        }
    }

    private final class OvertimeAccumulator {
        private int requestCount;
        private int pendingRequests;
        private int approvedRequests;
        private int rejectedRequests;
        private int cancelledRequests;
        private int requestedMinutes;
        private int approvedMinutes;
        private int actualMinutes;
        private int payableMinutes;

        private void addRequest(OvertimeRequest request) {
            requestCount++;
            requestedMinutes += request.getRequestedMinutes() == null ? 0 : request.getRequestedMinutes();
            String status = request.getStatus() == null ? "" : request.getStatus().trim().toUpperCase(Locale.ROOT);
            switch (status) {
                case "APPROVED" -> {
                    approvedRequests++;
                    approvedMinutes += request.getRequestedMinutes() == null ? 0 : request.getRequestedMinutes();
                }
                case "REJECTED" -> rejectedRequests++;
                case "CANCELLED" -> cancelledRequests++;
                default -> pendingRequests++;
            }
        }

        private void addAttendance(Attendance attendance) {
            actualMinutes += valueOrZero(attendance.getActualOvertimeMinutes());
            payableMinutes += valueOrZero(attendance.getPayableOvertimeMinutes());
        }

        private void addEmployeeSummary(EmployeeOvertimeSummaryResponse summary) {
            requestCount += summary.requestCount();
            pendingRequests += summary.pendingRequests();
            approvedRequests += summary.approvedRequests();
            rejectedRequests += summary.rejectedRequests();
            cancelledRequests += summary.cancelledRequests();
            requestedMinutes += summary.requestedMinutes();
            approvedMinutes += summary.approvedMinutes();
            actualMinutes += summary.actualMinutes();
            payableMinutes += summary.payableMinutes();
        }
    }
}
