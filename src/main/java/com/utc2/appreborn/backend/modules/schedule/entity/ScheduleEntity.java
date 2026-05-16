package com.utc2.appreborn.backend.modules.schedule.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "start_period")
    private Integer startPeriod;

    @Column(name = "end_period")
    private Integer endPeriod;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "room")
    private String room;

    @Column(name = "building")
    private String building;

    @Column(name = "lecturer_name")
    private String lecturerName;

    @Column(name = "week_start")
    private Integer weekStart;

    @Column(name = "week_end")
    private Integer weekEnd;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
