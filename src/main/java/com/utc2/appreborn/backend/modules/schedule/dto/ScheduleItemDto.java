package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDto {
    private String subjectCode;
    private String subjectName;
    private String type;         // "LÝ THUYẾT" / "THỰC HÀNH"
    private String lecturer;
    private int dayOfWeek;       // 0=Thứ 2 ... 5=Thứ 7
    private int startPeriod;
    private int endPeriod;
    private String startTime;    // "07:00"
    private String endTime;      // "09:30"
    private String startDate;    // "dd/MM/yyyy"
    private String endDate;
    private String room;
    private String building;
}
