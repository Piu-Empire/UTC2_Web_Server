package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.*;

/** Response cho mobile — tra lịch theo sinh viên (file JSON) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDto {

    private String subjectCode;
    private String subjectName;
    /** "LÝ THUYẾT" hoặc "THỰC HÀNH" */
    private String type;
    private String lecturer;

    private int dayOfWeek;
    private int startPeriod;
    private int endPeriod;
    private String startTime;   // "HH:mm"
    private String endTime;     // "HH:mm"
    private String startDate;   // "dd/MM/yyyy" — lấy từ SEMESTER
    private String endDate;

    private Integer weekStart;
    private Integer weekEnd;
    private String room;
    private String building;
    private Integer scheduleType;
    private String notes;
}
