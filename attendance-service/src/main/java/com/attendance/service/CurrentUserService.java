package com.attendance.service;

import com.attendance.client.HrClient;
import com.attendance.dto.response.EmployeeInternalResponse;
import com.attendance.exception.AppException;
import com.attendance.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final HrClient hrClient;

    public Long getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        String subject = authentication.getName();
        if (subject == null || !subject.chars().allMatch(Character::isDigit)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Token phải chứa userId dạng số");
        }

        EmployeeInternalResponse employee = hrClient.getEmployeeByUserId(Long.parseLong(subject));
        if (employee == null || employee.employeeId() == null) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return employee.employeeId();
    }
}
