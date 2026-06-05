package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.*;

/** Mobile gọi trước để check version — tránh download file không cần thiết */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMetaDto {
    private String studentCode;
    private String lastUpdated;   // ISO-8601
}
