package com.attendance.repository.spec;

import com.attendance.entity.Attendance;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AttendanceSpecifications {

    private AttendanceSpecifications() {}

    // Tạo điều kiện tìm kiếm chấm công theo nhân viên, ngày và trạng thái.
    public static Specification<Attendance> matches(
            Long employeeId,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            String status) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }

            if (date != null) {
                predicates.add(cb.equal(root.get("workDate"), date));
            } else {
                if (fromDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("workDate"), fromDate));
                }
                if (toDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("workDate"), toDate));
                }
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
