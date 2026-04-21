package com.attendance.service;

import com.attendance.entity.Attendance;
import com.attendance.entity.Employee;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.spec.EmployeeSpecifications;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceReportExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public byte[] exportMonthlyExcel(int year, int month, Long departmentId, Long employeeId, boolean includeDetails) {
        if (employeeId != null) {
            return exportEmployeeMonthlyExcel(year, month, employeeId);
        }

        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate fromDate = reportMonth.atDay(1);
        LocalDate toDate = reportMonth.atEndOfMonth();

        List<Employee> employees = loadReportEmployees(departmentId, employeeId);
        List<Attendance> attendances = attendanceRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(fromDate, toDate);
        Map<Long, Map<LocalDate, Attendance>> attendanceByEmployeeAndDate = groupByEmployeeAndDate(attendances);

        StringBuilder html = new StringBuilder(64_000);
        html.append('\ufeff');
        html.append("<html><head><meta charset=\"UTF-8\"><style>");
        html.append("table{border-collapse:collapse;font-family:Arial,sans-serif;font-size:12px}");
        html.append("th,td{border:1px solid #999;padding:4px;white-space:nowrap}");
        html.append("th{background:#d9eaf7;font-weight:bold;text-align:center}");
        html.append(".title{font-size:18px;font-weight:bold;text-align:center;background:#fff}");
        html.append(".summary{background:#f3f4f6;font-weight:bold}");
        html.append(".number{text-align:right}");
        html.append("</style></head><body>");
        html.append("<table>");
        int columnCount = includeDetails ? 10 + reportMonth.lengthOfMonth() : 10;
        html.append("<tr><th class=\"title\" colspan=\"").append(columnCount).append("\">");
        html.append("BẢNG TỔNG HỢP CHẤM CÔNG THÁNG ").append(month).append('/').append(year);
        html.append("</th></tr>");
        html.append("<tr><td colspan=\"").append(columnCount).append("\">");
        html.append("Từ ngày ").append(fromDate.format(DATE_FORMAT)).append(" đến ngày ").append(toDate.format(DATE_FORMAT));
        html.append("</td></tr>");

        appendHeader(html, reportMonth, includeDetails);

        int index = 1;
        for (Employee employee : employees) {
            appendEmployeeRow(html, index++, employee, reportMonth, attendanceByEmployeeAndDate.get(employee.getId()), includeDetails);
        }

        html.append("</table>");
        if (includeDetails) {
            html.append("<br><table>");
            html.append("<tr><th>Ký hiệu</th><th>Ý nghĩa</th></tr>");
            html.append("<tr><td>P</td><td>Có mặt đúng giờ</td></tr>");
            html.append("<tr><td>L</td><td>Đi muộn</td></tr>");
            html.append("<tr><td>E</td><td>Về sớm</td></tr>");
            html.append("<tr><td>LE</td><td>Đi muộn và về sớm</td></tr>");
            html.append("<tr><td>A</td><td>Vắng mặt</td></tr>");
            html.append("<tr><td>-</td><td>Chưa có dữ liệu chấm công</td></tr>");
            html.append("</table>");
        }
        html.append("</body></html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] exportEmployeeMonthlyExcel(int year, int month, Long employeeId) {
        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate fromDate = reportMonth.atDay(1);
        LocalDate toDate = reportMonth.atEndOfMonth();

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên"));
        List<Attendance> attendances = attendanceRepository.findByWorkDateBetweenOrderByEmployeeIdAscWorkDateAsc(fromDate, toDate);
        Map<LocalDate, Attendance> attendanceByDate = groupByEmployeeAndDate(attendances)
                .getOrDefault(employeeId, Map.of());
        MonthlyTotals totals = calculateTotals(reportMonth, attendanceByDate);

        StringBuilder html = new StringBuilder(32_000);
        html.append('\ufeff');
        html.append("<html><head><meta charset=\"UTF-8\"><style>");
        html.append("table{border-collapse:collapse;font-family:Arial,sans-serif;font-size:12px}");
        html.append("th,td{border:1px solid #999;padding:5px;white-space:nowrap}");
        html.append("th{background:#d9eaf7;font-weight:bold;text-align:center}");
        html.append(".title{font-size:18px;font-weight:bold;text-align:center;background:#fff}");
        html.append(".section{background:#f3f4f6;font-weight:bold}");
        html.append(".number{text-align:right}");
        html.append("</style></head><body>");

        html.append("<table>");
        html.append("<tr><th class=\"title\" colspan=\"9\">BẢNG CÔNG NHÂN VIÊN THÁNG ")
                .append(month).append('/').append(year).append("</th></tr>");
        appendInfoRow(html, "Mã nhân viên", employee.getEmployeeCode(), "Họ và tên", employee.getFullName());
        appendInfoRow(
                html,
                "Phòng ban",
                employee.getDepartment() == null ? "" : employee.getDepartment().getName(),
                "Chức vụ",
                employee.getPosition() == null ? "" : employee.getPosition().getName());
        html.append("<tr><td class=\"section\">Kỳ công</td><td colspan=\"8\">")
                .append(fromDate.format(DATE_FORMAT))
                .append(" - ")
                .append(toDate.format(DATE_FORMAT))
                .append("</td></tr>");
        html.append("<tr>");
        appendSummaryCell(html, "Tổng ngày công", totals.workDays());
        appendSummaryCell(html, "Đi muộn", totals.lateDays());
        appendSummaryCell(html, "Về sớm", totals.earlyLeaveDays());
        appendSummaryCell(html, "Vắng", totals.absentDays());
        html.append("<td class=\"section\">Tổng giờ</td><td class=\"number\">")
                .append(String.format("%.2f", totals.workedMinutes() / 60.0))
                .append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append("<br><table>");
        html.append("<tr>");
        html.append("<th>Ngày</th>");
        html.append("<th>Thứ</th>");
        html.append("<th>Giờ vào</th>");
        html.append("<th>Giờ ra</th>");
        html.append("<th>Trạng thái</th>");
        html.append("<th>Đi muộn</th>");
        html.append("<th>Về sớm</th>");
        html.append("<th>Tổng giờ</th>");
        html.append("<th>Ghi chú</th>");
        html.append("</tr>");

        for (int day = 1; day <= reportMonth.lengthOfMonth(); day++) {
            LocalDate date = reportMonth.atDay(day);
            appendEmployeeDailyRow(html, date, attendanceByDate.get(date));
        }

        html.append("</table>");
        html.append("</body></html>");

        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<Employee> loadReportEmployees(Long departmentId, Long employeeId) {
        if (employeeId != null) {
            return employeeRepository.findById(employeeId).stream().toList();
        }

        return employeeRepository.findAll(
                EmployeeSpecifications.matches(null, departmentId, null, "ACTIVE"),
                Sort.by(Sort.Direction.ASC, "fullName"));
    }

    private Map<Long, Map<LocalDate, Attendance>> groupByEmployeeAndDate(List<Attendance> attendances) {
        Map<Long, Map<LocalDate, Attendance>> result = new HashMap<>();
        for (Attendance attendance : attendances) {
            result.computeIfAbsent(attendance.getEmployeeId(), ignored -> new HashMap<>())
                    .put(attendance.getWorkDate(), attendance);
        }
        return result;
    }

    private void appendHeader(StringBuilder html, YearMonth reportMonth, boolean includeDetails) {
        html.append("<tr>");
        html.append("<th>STT</th>");
        html.append("<th>Mã NV</th>");
        html.append("<th>Họ và tên</th>");
        html.append("<th>Phòng ban</th>");
        html.append("<th>Chức vụ</th>");
        html.append("<th>Tổng ngày công</th>");
        html.append("<th>Đi muộn</th>");
        html.append("<th>Về sớm</th>");
        html.append("<th>Vắng</th>");
        html.append("<th>Tổng giờ</th>");
        if (includeDetails) {
            for (int day = 1; day <= reportMonth.lengthOfMonth(); day++) {
                html.append("<th>").append(day).append("</th>");
            }
        }
        html.append("</tr>");
    }

    private void appendInfoRow(StringBuilder html, String firstLabel, String firstValue, String secondLabel, String secondValue) {
        html.append("<tr>");
        html.append("<td class=\"section\">").append(escapeHtml(firstLabel)).append("</td>");
        appendCell(html, firstValue);
        html.append("<td class=\"section\">").append(escapeHtml(secondLabel)).append("</td>");
        html.append("<td colspan=\"6\">").append(escapeHtml(secondValue)).append("</td>");
        html.append("</tr>");
    }

    private void appendSummaryCell(StringBuilder html, String label, int value) {
        html.append("<td class=\"section\">").append(escapeHtml(label)).append("</td>");
        html.append("<td class=\"number\">").append(value).append("</td>");
    }

    private void appendEmployeeDailyRow(StringBuilder html, LocalDate date, Attendance attendance) {
        html.append("<tr>");
        appendCell(html, date.format(DATE_FORMAT));
        appendCell(html, dayOfWeekLabel(date));

        if (attendance == null) {
            appendCell(html, "");
            appendCell(html, "");
            appendCell(html, "Chưa có dữ liệu");
            appendNumberCell(html, "");
            appendNumberCell(html, "");
            appendNumberCell(html, "");
            appendCell(html, "");
            html.append("</tr>");
            return;
        }

        appendCell(html, attendance.getCheckInTime() == null ? "" : attendance.getCheckInTime().format(TIME_FORMAT));
        appendCell(html, attendance.getCheckOutTime() == null ? "" : attendance.getCheckOutTime().format(TIME_FORMAT));
        appendCell(html, statusLabel(attendance.getStatus()));
        appendNumberCell(html, attendance.getLateMinutes() == null ? "" : attendance.getLateMinutes());
        appendNumberCell(html, attendance.getEarlyLeaveMinutes() == null ? "" : attendance.getEarlyLeaveMinutes());
        appendNumberCell(html, attendance.getWorkedMinutes() == null ? "" : String.format("%.2f", attendance.getWorkedMinutes() / 60.0));
        appendCell(html, attendance.getCheckInTime() != null && attendance.getCheckOutTime() == null ? "Thiếu giờ ra" : "");
        html.append("</tr>");
    }

    private void appendEmployeeRow(
            StringBuilder html,
            int index,
            Employee employee,
            YearMonth reportMonth,
            Map<LocalDate, Attendance> attendanceByDate,
            boolean includeDetails) {
        MonthlyTotals totals = calculateTotals(reportMonth, attendanceByDate);

        html.append("<tr>");
        appendCell(html, String.valueOf(index));
        appendCell(html, employee.getEmployeeCode());
        appendCell(html, employee.getFullName());
        appendCell(html, employee.getDepartment() == null ? "" : employee.getDepartment().getName());
        appendCell(html, employee.getPosition() == null ? "" : employee.getPosition().getName());
        appendNumberCell(html, totals.workDays());
        appendNumberCell(html, totals.lateDays());
        appendNumberCell(html, totals.earlyLeaveDays());
        appendNumberCell(html, totals.absentDays());
        appendNumberCell(html, String.format("%.2f", totals.workedMinutes() / 60.0));

        if (includeDetails) {
            for (int day = 1; day <= reportMonth.lengthOfMonth(); day++) {
                LocalDate date = reportMonth.atDay(day);
                Attendance attendance = attendanceByDate == null ? null : attendanceByDate.get(date);
                appendCell(html, formatDailyCell(attendance));
            }
        }
        html.append("</tr>");
    }

    private MonthlyTotals calculateTotals(YearMonth reportMonth, Map<LocalDate, Attendance> attendanceByDate) {
        int workDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int absentDays = 0;
        int workedMinutes = 0;

        for (int day = 1; day <= reportMonth.lengthOfMonth(); day++) {
            Attendance attendance = attendanceByDate == null ? null : attendanceByDate.get(reportMonth.atDay(day));
            if (attendance == null) {
                continue;
            }

            String status = normalizeStatus(attendance.getStatus());
            if ("ABSENT".equals(status)) {
                absentDays++;
                continue;
            }

            if (attendance.getCheckInTime() != null) {
                workDays++;
            }
            if ("LATE".equals(status) || "LATE_AND_EARLY_LEAVE".equals(status)) {
                lateDays++;
            }
            if ("EARLY_LEAVE".equals(status) || "LATE_AND_EARLY_LEAVE".equals(status)) {
                earlyLeaveDays++;
            }
            workedMinutes += attendance.getWorkedMinutes() == null ? 0 : attendance.getWorkedMinutes();
        }

        return new MonthlyTotals(workDays, lateDays, earlyLeaveDays, absentDays, workedMinutes);
    }

    private String formatDailyCell(Attendance attendance) {
        if (attendance == null) {
            return "-";
        }

        String code = statusCode(attendance.getStatus());
        if (attendance.getCheckInTime() == null) {
            return code;
        }

        String checkIn = attendance.getCheckInTime().format(TIME_FORMAT);
        String checkOut = attendance.getCheckOutTime() == null ? "" : attendance.getCheckOutTime().format(TIME_FORMAT);
        return code + " " + checkIn + (checkOut.isBlank() ? "" : "-" + checkOut);
    }

    private String statusCode(String status) {
        return switch (normalizeStatus(status)) {
            case "LATE" -> "L";
            case "EARLY_LEAVE" -> "E";
            case "LATE_AND_EARLY_LEAVE" -> "LE";
            case "ABSENT" -> "A";
            default -> "P";
        };
    }

    private String statusLabel(String status) {
        return switch (normalizeStatus(status)) {
            case "LATE" -> "Đi muộn";
            case "EARLY_LEAVE" -> "Về sớm";
            case "LATE_AND_EARLY_LEAVE" -> "Đi muộn và về sớm";
            case "ABSENT" -> "Vắng mặt";
            case "PRESENT" -> "Có mặt";
            default -> status == null || status.isBlank() ? "Có dữ liệu" : status;
        };
    }

    private String dayOfWeekLabel(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Thứ 2";
            case TUESDAY -> "Thứ 3";
            case WEDNESDAY -> "Thứ 4";
            case THURSDAY -> "Thứ 5";
            case FRIDAY -> "Thứ 6";
            case SATURDAY -> "Thứ 7";
            case SUNDAY -> "Chủ nhật";
        };
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private void appendCell(StringBuilder html, String value) {
        html.append("<td>").append(escapeHtml(value)).append("</td>");
    }

    private void appendNumberCell(StringBuilder html, Object value) {
        html.append("<td class=\"number\">").append(value).append("</td>");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record MonthlyTotals(
            int workDays,
            int lateDays,
            int earlyLeaveDays,
            int absentDays,
            int workedMinutes) {
    }
}
