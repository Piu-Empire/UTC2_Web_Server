package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Cấu trúc file JSON mà app tải về và lưu local — key là studentCode (MSSV)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleFileDto {
    private String studentCode;
    private String lastUpdated;   // ISO-8601: "2026-05-16T10:00:00"
    private List<ScheduleItemDto> schedules;
}
