package com.utc2.appreborn.backend.modules.schedule.service;

import com.utc2.appreborn.backend.modules.schedule.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

public interface ScheduleService {

        // ── MOBILE ───────────────────────────────────────────────────────────────
        ScheduleMetaDto getMeta(String studentCode);

        ScheduleFileDto getScheduleFile(String studentCode);

        // ── ADMIN: tra theo lớp học phần ─────────────────────────────────────────
        Page<ScheduleResponse> getBySection(
                        Long sectionId, Long semesterId,
                        Integer scheduleType, String keyword,
                        Pageable pageable);

        // ── ADMIN: tra theo giảng viên ────────────────────────────────────────────
        Page<ScheduleResponse> getByLecturer(
                        Long lecturerId, String lecturerName,
                        Long semesterId, Integer scheduleType,
                        Pageable pageable);

        // ── ADMIN: toàn bộ lịch ──────────────────────────────────────────────────
        Page<ScheduleResponse> getAll(
                        Long semesterId,
                        Integer scheduleType,
                        Long lecturerId,
                        String sectionCode,
                        String courseName,
                        Integer dayOfWeek,
                        Integer period,
                        String room,
                        String lecturerName,
                        Pageable pageable);

        // ── ADMIN: upsert ─────────────────────────────────────────────────────────
        ScheduleResponse upsert(ScheduleRequest request);

        List<ScheduleResponse> bulkUpsert(List<ScheduleRequest> requests);

        // ── ADMIN: CRUD ───────────────────────────────────────────────────────────
        ScheduleResponse getById(Long id);

        ScheduleResponse create(ScheduleRequest request);

        ScheduleResponse update(Long id, ScheduleRequest request);

        void delete(Long id);

        // ── ADMIN: Import / Export ────────────────────────────────────────────────
        ImportResultDto importExcel(MultipartFile file, Integer scheduleType, boolean overwrite) throws IOException;

        void exportExcel(Long semesterId, Integer scheduleType,
                        String lecturerName, String room,
                        String sectionCode, String courseName,
                        Integer dayOfWeek, Integer weekStart, Integer weekEnd,
                        HttpServletResponse response) throws IOException;
}