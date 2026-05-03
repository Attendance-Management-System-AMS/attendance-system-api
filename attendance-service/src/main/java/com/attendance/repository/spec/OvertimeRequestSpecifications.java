package com.attendance.repository.spec;

import com.attendance.entity.OvertimeRequest;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class OvertimeRequestSpecifications {

    private OvertimeRequestSpecifications() {
    }

    public static Specification<OvertimeRequest> matches(
            String keyword,
            Long employeeId,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (employeeId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("employeeId"), employeeId));
            }
            if (StringUtils.hasText(status)) {
                predicate = cb.and(predicate, cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }
            if (fromDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("workDate"), fromDate));
            }
            if (toDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("workDate"), toDate));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("reason")), like));
            }

            return predicate;
        };
    }
}
