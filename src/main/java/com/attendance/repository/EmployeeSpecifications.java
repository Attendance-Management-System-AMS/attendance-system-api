package com.attendance.repository;

import com.attendance.entity.Employee;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EmployeeSpecifications {

    // Không cho khởi tạo vì đây là lớp chứa tiêu chí tìm kiếm tĩnh.
    private EmployeeSpecifications() {}

    // Tạo điều kiện lọc nhân viên theo nhiều tham số.
    public static Specification<Employee> matches(
            String keyword, Long departmentId, Long positionId, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("employeeCode")), pattern),
                        cb.and(
                                cb.isNotNull(root.get("email")),
                                cb.like(cb.lower(root.get("email")), pattern))));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            if (positionId != null) {
                predicates.add(cb.equal(root.get("position").get("id"), positionId));
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




