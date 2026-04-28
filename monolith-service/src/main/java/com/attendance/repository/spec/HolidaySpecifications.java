package com.attendance.repository.spec;

import com.attendance.entity.Holiday;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class HolidaySpecifications {

    private HolidaySpecifications() {}

    // Tạo điều kiện tìm kiếm ngày nghỉ theo tên, trạng thái trả lương và khoảng ngày.
    public static Specification<Holiday> matches(
            String keyword,
            Boolean isPaid,
            LocalDate fromDate,
            LocalDate toDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("holidayName")), pattern));
            }

            if (isPaid != null) {
                predicates.add(cb.equal(root.get("isPaid"), isPaid));
            }

            // overlap range: holiday.toDate >= fromDate AND holiday.fromDate <= toDate
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("toDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fromDate"), toDate));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
