package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "schedule_template_items")
@Getter
@Setter
public class ScheduleTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ScheduleTemplate template;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1-7, 1 is Monday, 7 is Sunday

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
}



