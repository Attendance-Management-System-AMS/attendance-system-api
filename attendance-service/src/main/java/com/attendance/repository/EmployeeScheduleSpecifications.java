package com.attendance.repository;

import com.attendance.entity.EmployeeSchedule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeeScheduleSpecifications {

    private EmployeeScheduleSpecifications() {}

    public static Specification<EmployeeSchedule> matches(
            Long employeeId,
            Integer dayOfWeek,
            Boolean isActive,
            LocalDate effectiveFromOnOrBefore,
            Long shiftId) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }
            if (dayOfWeek != null) {
                predicates.add(cb.equal(root.get("dayOfWeek"), dayOfWeek));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            if (effectiveFromOnOrBefore != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("effectiveFrom"), effectiveFromOnOrBefore));
            }
            if (shiftId != null) {
                predicates.add(cb.equal(root.get("shift").get("id"), shiftId));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}

