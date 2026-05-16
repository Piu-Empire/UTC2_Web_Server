package com.utc2.appreborn.backend.modules.schedule.service;

import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleFileDto;
import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleMetaDto;

public interface ScheduleService {
    ScheduleMetaDto getMeta(String studentCode);
    ScheduleFileDto getScheduleFile(String studentCode);
}
