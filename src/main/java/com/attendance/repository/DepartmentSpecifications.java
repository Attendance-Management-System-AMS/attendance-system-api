package com.attendance.repository;

import com.attendance.entity.Department;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class DepartmentSpecifications {

    // Không cho khởi tạo vì đây là lớp chứa tiêu chí tìm kiếm tĩnh.
    private DepartmentSpecifications() {}

    // Tạo điều kiện lọc phòng ban theo từ khoá.
    public static Specification<Department> matches(String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.and(
                                cb.isNotNull(root.get("description")),
                                cb.like(cb.lower(root.get("description")), pattern))));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}




