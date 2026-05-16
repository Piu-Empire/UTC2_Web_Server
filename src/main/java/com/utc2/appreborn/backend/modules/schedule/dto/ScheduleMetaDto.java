package com.utc2.appreborn.backend.modules.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// App gọi endpoint này trước — chỉ lấy timestamp để quyết định có cần download file mới không
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleMetaDto {
    private String studentCode;
    private String lastUpdated;  // ISO-8601
}
