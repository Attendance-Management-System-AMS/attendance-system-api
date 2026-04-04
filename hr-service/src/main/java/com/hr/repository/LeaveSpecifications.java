package com.hr.repository;

import com.hr.entity.Employee;
import com.hr.entity.LeaveRequest;
import com.hr.entity.LeaveType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class LeaveSpecifications {

    // Không cho khởi tạo vì đây là lớp chứa tiêu chí tìm kiếm tĩnh.
    private LeaveSpecifications() {}

    // Tạo điều kiện lọc đơn nghỉ theo từ khoá, nhân viên và trạng thái.
    public static Specification<LeaveRequest> matches(String keyword, Long employeeId, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<LeaveRequest, Employee> employeeJoin = null;
            Join<LeaveRequest, LeaveType> leaveTypeJoin = null;
            
            if (StringUtils.hasText(keyword)) {
                employeeJoin = root.join("employee");
                leaveTypeJoin = root.join("leaveType");
            }

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(leaveTypeJoin.get("name")), pattern),
                        cb.like(cb.lower(leaveTypeJoin.get("code")), pattern),
                        cb.and(
                                cb.isNotNull(root.get("reason")),
                                cb.like(cb.lower(root.get("reason")), pattern)),
                        cb.like(cb.lower(employeeJoin.get("fullName")), pattern)));
            }
            if (employeeId != null) {
                if (employeeJoin != null) {
                    predicates.add(cb.equal(employeeJoin.get("id"), employeeId));
                } else {
                    predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
                }
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
