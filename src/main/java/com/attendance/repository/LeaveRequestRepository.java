package com.attendance.repository;

import com.attendance.entity.LeaveRequest;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    // Nạp trước quan hệ nhân viên và người duyệt khi tìm danh sách đơn nghỉ.
    @EntityGraph(attributePaths = {"employee", "approvedBy"})
    @Override
    Page<LeaveRequest> findAll(Specification<LeaveRequest> spec, Pageable pageable);

    // Lấy danh sách đơn nghỉ theo nhân viên.
    List<LeaveRequest> findByEmployeeId(Long employeeId);

    // Lấy danh sách đơn nghỉ theo trạng thái.
    List<LeaveRequest> findByStatus(String status);

    boolean existsByEmployeeIdAndStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId,
            String status,
            LocalDate fromDate,
            LocalDate toDate);

    boolean existsByEmployeeIdAndStatusInAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId,
            Collection<String> statuses,
            LocalDate fromDate,
            LocalDate toDate);
}




