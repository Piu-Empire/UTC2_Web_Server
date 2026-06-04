package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** Response trả về cho admin web (danh sách + chi tiết) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {

    private Long scheduleId;
    private Long userId;

    // ── CLASS_SECTION info (join) ────────────────────────────────────────────
    private Long sectionId;
    /** Mã lớp học phần. VD: MATH101-01 */
    private String sectionCode;

    // ── COURSE info (join qua CLASS_SECTION) ────────────────────────────────
    private Long courseId;
    private String courseCode;
    private String courseName;

    // ── SEMESTER info (join qua CLASS_SECTION) ───────────────────────────────
    private Long semesterId;
    private String semesterName;

    // ── Lịch ────────────────────────────────────────────────────────────────
    private Integer scheduleType;   // 1/2/3
    private Integer dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private String building;
    private String lecturerName;
    private Long lecturerId;
    private Integer weekStart;
    private Integer weekEnd;
    private String notes;
    private LocalDateTime updatedAt;
}
