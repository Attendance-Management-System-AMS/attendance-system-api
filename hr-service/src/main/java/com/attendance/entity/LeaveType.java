package com.attendance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @Column(name = "deduct_annual_leave", nullable = false)
    private Boolean deductAnnualLeave;

    @Column(name = "insurance_covers", nullable = false)
    private Boolean insuranceCovers;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(length = 500)
    private String description;
}
