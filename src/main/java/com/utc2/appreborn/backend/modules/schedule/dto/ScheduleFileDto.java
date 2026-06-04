package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.*;
import java.util.List;

/** File JSON mà mobile tải về và lưu local — key là studentCode */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleFileDto {
    private String studentCode;
    private String lastUpdated;     // ISO-8601
    private List<ScheduleItemDto> schedules;
}
