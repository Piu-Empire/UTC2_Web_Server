package com.utc2.appreborn.backend.modules.schedule.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleFileDto;
import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleItemDto;
import com.utc2.appreborn.backend.modules.schedule.dto.ScheduleMetaDto;
import com.utc2.appreborn.backend.modules.schedule.entity.ScheduleEntity;
import com.utc2.appreborn.backend.modules.schedule.repository.ScheduleRepository;
import com.utc2.appreborn.backend.modules.schedule.service.ScheduleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final DateTimeFormatter ISO      = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public ScheduleMetaDto getMeta(String studentCode) {
        LocalDateTime lastUpdated = scheduleRepository.findLastUpdatedByStudentCode(studentCode)
                .orElse(LocalDateTime.now());
        return ScheduleMetaDto.builder()
                .studentCode(studentCode)
                .lastUpdated(lastUpdated.format(ISO))
                .build();
    }

    @Override
    public ScheduleFileDto getScheduleFile(String studentCode) {
        List<ScheduleEntity> schedules = scheduleRepository.findByStudentCode(studentCode);

        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy lịch học cho sinh viên " + studentCode);
        }

        LocalDateTime lastUpdated = schedules.stream()
                .map(ScheduleEntity::getUpdatedAt)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        List<ScheduleItemDto> items = schedules.stream()
                .map(this::mapToDto)
                .toList();

        return ScheduleFileDto.builder()
                .studentCode(studentCode)
                .lastUpdated(lastUpdated.format(ISO))
                .schedules(items)
                .build();
    }

    private ScheduleItemDto mapToDto(ScheduleEntity s) {
        Object[] courseRow = (Object[]) entityManager
                .createNativeQuery("SELECT course_code, course_name, theory_hours FROM course WHERE course_id = :id")
                .setParameter("id", s.getCourseId())
                .getSingleResult();

        String courseCode = (String) courseRow[0];
        String courseName = (String) courseRow[1];
        int theoryHours   = ((Number) courseRow[2]).intValue();
        String type       = theoryHours > 0 ? "LÝ THUYẾT" : "THỰC HÀNH";

        return ScheduleItemDto.builder()
                .subjectCode(courseCode)
                .subjectName(courseName)
                .type(type)
                .lecturer(s.getLecturerName())
                .dayOfWeek(s.getDayOfWeek() != null ? s.getDayOfWeek() : 0)
                .startPeriod(s.getStartPeriod() != null ? s.getStartPeriod() : 0)
                .endPeriod(s.getEndPeriod() != null ? s.getEndPeriod() : 0)
                .startTime(s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : "")
                .endTime(s.getEndTime() != null ? s.getEndTime().format(TIME_FMT) : "")
                .startDate(getSemesterDate(s.getSemesterId(), "start_date"))
                .endDate(getSemesterDate(s.getSemesterId(), "end_date"))
                .room(s.getRoom())
                .building(s.getBuilding())
                .build();
    }

    private String getSemesterDate(Long semesterId, String column) {
        try {
            Object result = entityManager
                    .createNativeQuery("SELECT " + column + " FROM semester WHERE semester_id = :id")
                    .setParameter("id", semesterId)
                    .getSingleResult();
            if (result instanceof java.sql.Date sqlDate) {
                return sqlDate.toLocalDate().format(DATE_FMT);
            }
            return result.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
