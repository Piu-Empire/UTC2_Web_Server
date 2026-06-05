package com.utc2.appreborn.backend.modules.schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Maps to bảng SCHEDULE trong ERD.
 *
 * Quan hệ:
 * schedule.user_id → USER (người tạo / chủ lịch — sinh viên hoặc admin)
 * schedule.section_id → CLASS_SECTION
 */
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

    /** FK → USER (người sở hữu lịch, thường là sinh viên) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FK → CLASS_SECTION — lớp học phần tương ứng */
    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    /** Thứ trong tuần: 2–7 = Thứ 2–7, 8 = Chủ nhật */
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    /** Tiết bắt đầu (1–12) */
    @Column(name = "start_period")
    private Integer startPeriod;

    /** Tiết kết thúc (1–12) */
    @Column(name = "end_period")
    private Integer endPeriod;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "room", length = 50)
    private String room;

    @Column(name = "building", length = 100)
    private String building;

    /** Tên giảng viên (denormalized để tránh join nặng khi render mobile) */
    @Column(name = "lecturer_name", length = 255)
    private String lecturerName;

    @Column(name = "lecturer_id")
    private Long lecturerId;

    /** Tuần bắt đầu áp dụng trong học kỳ */
    @Column(name = "week_start")
    private Integer weekStart;

    /** Tuần kết thúc áp dụng trong học kỳ */
    @Column(name = "week_end")
    private Integer weekEnd;
 
    /* 
     * ại lịch:
     *   1 = Lịch học (thường kỳ)
     *   2 = Lịch thi
     *   3 = Lịch thi lại
     */
    @Column(name = "schedule_type", nullable = false)
    private Integer scheduleType;

    @Column(name = "exam_date_start")
    private java.time.LocalDate examDateStart;

    @Column(name = "exam_date_end")
    private java.time.LocalDate examDateEnd;

    @Column(name = "notes")
    private String notes;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
