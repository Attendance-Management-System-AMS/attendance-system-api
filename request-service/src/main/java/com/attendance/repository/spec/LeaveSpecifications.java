package com.attendance.repository.spec;

import com.attendance.entity.LeaveRequest;
import com.attendance.entity.LeaveType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class LeaveSpecifications {

    private LeaveSpecifications() {
    }

    public static Specification<LeaveRequest> matches(String keyword, Long employeeId, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<LeaveRequest, LeaveType> leaveTypeJoin = null;

            if (StringUtils.hasText(keyword)) {
                leaveTypeJoin = root.join("leaveType");
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(leaveTypeJoin.get("name")), pattern),
                        cb.like(cb.lower(leaveTypeJoin.get("code")), pattern),
                        cb.and(
                                cb.isNotNull(root.get("reason")),
                                cb.like(cb.lower(root.get("reason")), pattern))));
            }

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
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
