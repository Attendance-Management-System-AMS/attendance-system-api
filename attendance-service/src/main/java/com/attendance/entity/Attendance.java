package com.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "work_date", nullable = false)
    private java.time.LocalDate workDate;

    @Column(name = "status")
    private String status; // PRESENT, LATE, EARLY_LEAVE, ABSENT

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "early_leave_minutes")
    private Integer earlyLeaveMinutes;

    @Column(name = "worked_minutes")
    private Integer workedMinutes;

    @Column(name = "expected_minutes")
    private Integer expectedMinutes;

    @Column(name = "actual_overtime_minutes", nullable = false)
    @Builder.Default
    private Integer actualOvertimeMinutes = 0;

    @Column(name = "approved_overtime_minutes", nullable = false)
    @Builder.Default
    private Integer approvedOvertimeMinutes = 0;

    @Column(name = "payable_overtime_minutes", nullable = false)
    @Builder.Default
    private Integer payableOvertimeMinutes = 0;

    @Column(name = "overtime_status", nullable = false)
    @Builder.Default
    private String overtimeStatus = "NONE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}



