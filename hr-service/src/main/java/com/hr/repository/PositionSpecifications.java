package com.hr.repository;

import com.hr.entity.Position;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PositionSpecifications {

    // Không cho khởi tạo vì đây là lớp chứa tiêu chí tìm kiếm tĩnh.
    private PositionSpecifications() {}

    // Tạo điều kiện lọc chức vụ theo từ khoá và phòng ban.
    public static Specification<Position> matches(String keyword, Long departmentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
