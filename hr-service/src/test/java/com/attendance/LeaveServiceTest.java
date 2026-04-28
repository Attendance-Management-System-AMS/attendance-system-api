package com.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.attendance.client.AttendanceClient;
import com.attendance.common.dto.LeaveApprovalSyncRequest;
import com.attendance.dto.response.LeaveResponse;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveType;
import com.attendance.exception.AppException;
import com.attendance.mapper.LeaveMapper;
import com.attendance.mapper.LeaveTypeMapper;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.LeaveRequestRepository;
import com.attendance.repository.LeaveTypeRepository;
import com.attendance.service.LeaveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveTypeRepository leaveTypeRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveMapper leaveMapper;

    @Mock
    private LeaveTypeMapper leaveTypeMapper;

    @Mock
    private AttendanceClient attendanceClient;

    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        leaveService = new LeaveService(
                leaveRequestRepository,
                leaveTypeRepository,
                employeeRepository,
                leaveMapper,
                leaveTypeMapper,
                attendanceClient,
                new ObjectMapper());
    }

    @Test
    void approveSyncsAttendanceWithRequestBody() {
        LeaveRequest leaveRequest = createPendingLeave();
        Employee employee = createEmployee(4L, "EMP-0004", "Nguyen Van A");
        LeaveResponse response = new LeaveResponse(
                1L,
                4L,
                "Nguyen Van A",
                "EMP-0004",
                "Phòng Nhân sự",
                "Chuyên viên",
                "AL",
                "Nghỉ phép năm",
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getTotalDays(),
                leaveRequest.getReason(),
                "APPROVED",
                null,
                LocalDateTime.now());

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        when(leaveMapper.toResponse(any(LeaveRequest.class), any(), any())).thenReturn(response);

        LeaveResponse approved = leaveService.approve(1L, null);

        assertEquals("APPROVED", approved.status());
        verify(attendanceClient)
                .syncApprovedLeave(new LeaveApprovalSyncRequest(4L, LocalDate.of(2026, 4, 25), LocalDate.of(2026, 4, 25)));
    }

    @Test
    void approveSurfacesRemoteAttendanceErrorMessage() {
        LeaveRequest leaveRequest = createPendingLeave();
        Employee employee = createEmployee(4L, "EMP-0004", "Nguyen Van A");

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.findById(4L)).thenReturn(Optional.of(employee));
        doThrow(buildFeignException(400, "{\"success\":false,\"code\":1002,\"message\":\"Ngày kết thúc phải sau ngày bắt đầu\"}"))
                .when(attendanceClient)
                .syncApprovedLeave(any(LeaveApprovalSyncRequest.class));

        AppException exception = assertThrows(AppException.class, () -> leaveService.approve(1L, null));

        assertEquals("Ngày kết thúc phải sau ngày bắt đầu", exception.getMessage());
        assertEquals(1002, exception.getErrorCode().getCode());
    }

    private LeaveRequest createPendingLeave() {
        LeaveType leaveType = new LeaveType();
        leaveType.setId(1L);
        leaveType.setCode("AL");
        leaveType.setName("Nghỉ phép năm");

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(1L);
        leaveRequest.setEmployeeId(4L);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setFromDate(LocalDate.of(2026, 4, 25));
        leaveRequest.setToDate(LocalDate.of(2026, 4, 25));
        leaveRequest.setTotalDays(1.0);
        leaveRequest.setReason("Việc riêng");
        leaveRequest.setStatus("PENDING");
        return leaveRequest;
    }

    private Employee createEmployee(Long id, String employeeCode, String fullName) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(employeeCode);
        employee.setFullName(fullName);
        return employee;
    }

    private FeignException buildFeignException(int status, String body) {
        Request request = Request.create(
                Request.HttpMethod.POST,
                "/internal/attendance/leave-approvals/sync",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null);
        Response response = Response.builder()
                .status(status)
                .reason("Remote error")
                .request(request)
                .headers(Map.of())
                .body(body, StandardCharsets.UTF_8)
                .build();
        return FeignException.errorStatus("AttendanceClient#syncApprovedLeave", response);
    }
}
