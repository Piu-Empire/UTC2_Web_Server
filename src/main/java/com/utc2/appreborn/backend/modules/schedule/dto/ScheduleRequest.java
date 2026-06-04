package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.*;

import java.time.LocalTime;

/** Dùng cho: danh sách (GET /admin/schedules), tạo mới, cập nhật */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    private Long userId;

    /** FK → CLASS_SECTION */
    private Long sectionId;

    private Integer dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private String building;
    private String lecturerName;
    private Integer weekStart;
    private Integer weekEnd;

    /** 1=Lịch học | 2=Lịch thi | 3=Lịch thi lại */
    private Integer scheduleType;
    private String notes;
}
