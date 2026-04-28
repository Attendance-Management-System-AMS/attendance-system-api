package com.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code; // AL, PH, SL, ML, UL, BT

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Tên loại nghỉ

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid; // Hưởng lương

    @Column(name = "deduct_annual_leave", nullable = false)
    private Boolean deductAnnualLeave; // Trừ phép năm

    @Column(name = "insurance_covers", nullable = false)
    private Boolean insuranceCovers; // Bảo hiểm trả

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(length = 500)
    private String description; // Mô tả thêm
}




