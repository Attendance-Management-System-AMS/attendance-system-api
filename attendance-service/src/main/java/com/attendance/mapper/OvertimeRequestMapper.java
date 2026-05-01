package com.attendance.mapper;

import com.attendance.dto.response.HrEmployeeSnapshot;
import com.attendance.dto.response.OvertimeRequestResponse;
import com.attendance.entity.Attendance;
import com.attendance.entity.OvertimeRequest;
import org.springframework.stereotype.Component;

@Component
public class OvertimeRequestMapper {

    public OvertimeRequestResponse toResponse(
            OvertimeRequest request,
            HrEmployeeSnapshot employee,
            HrEmployeeSnapshot approvedBy,
            Attendance attendance) {
        return new OvertimeRequestResponse(
                request.getId(),
                request.getEmployeeId(),
                employee == null ? null : employee.fullName(),
                employee == null ? null : employee.employeeCode(),
                employee == null ? null : employee.departmentName(),
                employee == null ? null : employee.positionName(),
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getRequestedMinutes(),
                request.getReason(),
                request.getStatus(),
                request.getApprovedById(),
                approvedBy == null ? null : approvedBy.fullName(),
                request.getApprovedAt(),
                request.getApprovalNote(),
                attendance != null,
                attendance == null ? null : attendance.getStatus(),
                attendance == null ? 0 : attendance.getActualOvertimeMinutes(),
                attendance == null ? 0 : attendance.getApprovedOvertimeMinutes(),
                attendance == null ? 0 : attendance.getPayableOvertimeMinutes(),
                attendance == null ? "NONE" : attendance.getOvertimeStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }
}
