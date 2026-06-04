package com.utc2.appreborn.backend.modules.schedule.service.impl;

import com.utc2.appreborn.backend.exception.ResourceNotFoundException;
import com.utc2.appreborn.backend.modules.schedule.dto.*;
import com.utc2.appreborn.backend.modules.schedule.entity.ScheduleEntity;
import com.utc2.appreborn.backend.modules.schedule.repository.ScheduleRepository;
import com.utc2.appreborn.backend.modules.schedule.service.ScheduleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @PersistenceContext
    private EntityManager em;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ════════════════════════════════════════════════════════════════════════
    // MOBILE
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public ScheduleMetaDto getMeta(String studentCode) {
        LocalDateTime last = scheduleRepository
                .findLastUpdatedByStudentCode(studentCode)
                .orElse(LocalDateTime.now());
        return ScheduleMetaDto.builder()
                .studentCode(studentCode)
                .lastUpdated(last.format(ISO))
                .build();
    }

    @Override
    public ScheduleFileDto getScheduleFile(String studentCode) {
        List<ScheduleEntity> list = scheduleRepository.findByStudentCode(studentCode);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy lịch học cho sinh viên " + studentCode);
        }
        LocalDateTime last = list.stream()
                .map(ScheduleEntity::getUpdatedAt)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        return ScheduleFileDto.builder()
                .studentCode(studentCode)
                .lastUpdated(last.format(ISO))
                .schedules(list.stream().map(this::toItemDto).toList())
                .build();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — toàn bộ lịch
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public Page<ScheduleResponse> getAll(
            Long semesterId,
            Integer scheduleType,
            Long lecturerId,
            String sectionCode,
            String courseName,
            Integer dayOfWeek,
            Integer period,
            String room,
            String lecturerName,
            Pageable pageable) {

        return scheduleRepository
                .findAllPaged(
                        semesterId,
                        scheduleType,
                        lecturerId,
                        blankToNull(sectionCode),
                        blankToNull(courseName),
                        dayOfWeek,
                        period,
                        blankToNull(room),
                        blankToNull(lecturerName),
                        pageable)
                .map(this::toResponse);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — upsert (1 bản ghi)
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ScheduleResponse upsert(ScheduleRequest req) {
        ScheduleEntity entity = scheduleRepository
                .findForUpsert(req.getSectionId(), req.getScheduleType(),
                        req.getDayOfWeek(), req.getStartPeriod())
                .orElseGet(ScheduleEntity::new);

        entity.setUserId(req.getUserId());
        entity.setSectionId(req.getSectionId());
        entity.setDayOfWeek(req.getDayOfWeek());
        entity.setStartPeriod(req.getStartPeriod());
        entity.setEndPeriod(req.getEndPeriod());
        entity.setStartTime(req.getStartTime());
        entity.setEndTime(req.getEndTime());
        entity.setRoom(req.getRoom());
        entity.setBuilding(req.getBuilding());
        entity.setLecturerName(req.getLecturerName());
        entity.setLecturerId(resolveLecturerId(req.getLecturerName()));
        entity.setWeekStart(req.getWeekStart());
        entity.setWeekEnd(req.getWeekEnd());
        entity.setScheduleType(req.getScheduleType());
        entity.setNotes(req.getNotes());
        return toResponse(scheduleRepository.save(entity));
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — bulk upsert (danh sách)
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @Transactional
    public List<ScheduleResponse> bulkUpsert(List<ScheduleRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — tra theo lớp học phần
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public Page<ScheduleResponse> getBySection(Long sectionId, Long semesterId,
            Integer scheduleType, String keyword,
            Pageable pageable) {
        return scheduleRepository
                .findBySection(sectionId, semesterId, scheduleType,
                        blankToNull(keyword), pageable)
                .map(this::toResponse);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — tra theo giảng viên
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public Page<ScheduleResponse> getByLecturer(Long lecturerId, String lecturerName,
            Long semesterId, Integer scheduleType,
            Pageable pageable) {
        return scheduleRepository
                .findByLecturer(lecturerId, blankToNull(lecturerName),
                        semesterId, scheduleType, pageable)
                .map(this::toResponse);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADMIN — CRUD
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public ScheduleResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ScheduleResponse create(ScheduleRequest req) {
        ScheduleEntity entity = ScheduleEntity.builder()
                .userId(req.getUserId())
                .sectionId(req.getSectionId())
                .dayOfWeek(req.getDayOfWeek())
                .startPeriod(req.getStartPeriod())
                .endPeriod(req.getEndPeriod())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .room(req.getRoom())
                .building(req.getBuilding())
                .lecturerName(req.getLecturerName())
                .lecturerId(resolveLecturerId(req.getLecturerName()))
                .weekStart(req.getWeekStart())
                .weekEnd(req.getWeekEnd())
                .scheduleType(req.getScheduleType())
                .notes(req.getNotes())
                .build();
        return toResponse(scheduleRepository.save(entity));
    }

    @Override
    @Transactional
    public ScheduleResponse update(Long id, ScheduleRequest req) {
        ScheduleEntity entity = findOrThrow(id);
        entity.setUserId(req.getUserId());
        entity.setSectionId(req.getSectionId());
        entity.setDayOfWeek(req.getDayOfWeek());
        entity.setStartPeriod(req.getStartPeriod());
        entity.setEndPeriod(req.getEndPeriod());
        entity.setStartTime(req.getStartTime());
        entity.setEndTime(req.getEndTime());
        entity.setRoom(req.getRoom());
        entity.setBuilding(req.getBuilding());
        entity.setLecturerName(req.getLecturerName());
        entity.setLecturerId(resolveLecturerId(req.getLecturerName()));
        entity.setWeekStart(req.getWeekStart());
        entity.setWeekEnd(req.getWeekEnd());
        entity.setScheduleType(req.getScheduleType());
        entity.setNotes(req.getNotes());
        return toResponse(scheduleRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        scheduleRepository.delete(findOrThrow(id));
    }

    // ════════════════════════════════════════════════════════════════════════
    // IMPORT Excel
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Cột Excel (0-indexed):
     * 0 section_code (tra ra section_id)
     * 1 user_id
     * 2 day_of_week
     * 3 start_period
     * 4 end_period
     * 5 start_time (HH:mm)
     * 6 end_time (HH:mm)
     * 7 room
     * 8 building
     * 9 lecturer_name (server tự tra lecturer_id từ user_profile)
     * 10 week_start
     * 11 week_end
     * 12 notes
     */
    @Override
    @Transactional
    public void importExcel(MultipartFile file, Integer scheduleType, boolean overwrite) throws IOException {
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                String sectionCode = cellStr(row, 0);
                Long sectionId = resolveSectionId(sectionCode);
                if (sectionId == null)
                    continue; // bỏ qua dòng không tìm được lớp

                Long userId = cellLong(row, 1);

                if (overwrite) {
                    // Xóa bản ghi cũ cùng section + scheduleType + day + period
                    em.createNativeQuery("""
                            DELETE FROM schedule
                            WHERE section_id = :sid
                              AND schedule_type = :type
                              AND day_of_week = :dow
                              AND start_period = :sp
                            """)
                            .setParameter("sid", sectionId)
                            .setParameter("type", scheduleType)
                            .setParameter("dow", cellInt(row, 2))
                            .setParameter("sp", cellInt(row, 3))
                            .executeUpdate();
                }

                String lecturerName = cellStr(row, 9);
                ScheduleEntity e = ScheduleEntity.builder()
                        .userId(userId)
                        .sectionId(sectionId)
                        .dayOfWeek(cellInt(row, 2))
                        .startPeriod(cellInt(row, 3))
                        .endPeriod(cellInt(row, 4))
                        .startTime(parseTime(cellStr(row, 5)))
                        .endTime(parseTime(cellStr(row, 6)))
                        .room(cellStr(row, 7))
                        .building(cellStr(row, 8))
                        .lecturerName(lecturerName)
                        .lecturerId(resolveLecturerId(lecturerName))
                        .weekStart(cellInt(row, 10))
                        .weekEnd(cellInt(row, 11))
                        .scheduleType(scheduleType)
                        .notes(cellStr(row, 12))
                        .build();
                scheduleRepository.save(e);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // EXPORT Excel
    // ════════════════════════════════════════════════════════════════════════

    @Override
    @SuppressWarnings("unchecked")
    public void exportExcel(Long semesterId, Integer scheduleType,
            String lecturerName, String room,
            String sectionCode, String courseName,
            Integer dayOfWeek, Integer weekStart, Integer weekEnd,
            HttpServletResponse response) throws IOException {

        Long lecturerId = resolveLecturerId(lecturerName);

        List<ScheduleEntity> rows = scheduleRepository
                .findForExport(semesterId, scheduleType, lecturerId, blankToNull(room),
                        blankToNull(sectionCode), blankToNull(courseName),
                        dayOfWeek, weekStart, weekEnd);

        String academicYear = "2025-2026";
        Integer semesterNumber = 1;
        LocalDate semesterStartDate = null;

        if (semesterId != null) {
            try {
                Object[] semData = (Object[]) em.createNativeQuery(
                        "SELECT academic_year, semester_number, start_date FROM semester WHERE semester_id = :semId LIMIT 1")
                        .setParameter("semId", semesterId)
                        .getSingleResult();
                academicYear = (String) semData[0];
                semesterNumber = ((Number) semData[1]).intValue();
                if (semData[2] instanceof java.sql.Date d) {
                    semesterStartDate = d.toLocalDate();
                } else if (semData[2] instanceof java.time.LocalDate ld) {
                    semesterStartDate = ld;
                }
            } catch (Exception ignored) {
            }
        } else if (!rows.isEmpty()) {
            try {
                Object[] semData = (Object[]) em.createNativeQuery("""
                        SELECT sem.semester_id, sem.academic_year, sem.semester_number, sem.start_date
                        FROM semester sem
                        JOIN class_section cs ON cs.semester_id = sem.semester_id
                        WHERE cs.section_id = :sid LIMIT 1
                        """)
                        .setParameter("sid", rows.get(0).getSectionId())
                        .getSingleResult();
                semesterId = ((Number) semData[0]).longValue();
                academicYear = (String) semData[1];
                semesterNumber = ((Number) semData[2]).intValue();
                if (semData[3] instanceof java.sql.Date d) {
                    semesterStartDate = d.toLocalDate();
                } else if (semData[3] instanceof java.time.LocalDate ld) {
                    semesterStartDate = ld;
                }
            } catch (Exception ignored) {
            }
        }

        String templateName = (scheduleType != null && (scheduleType == 2 || scheduleType == 3))
                ? "templates/template_Lich_Thi.xls"
                : "templates/template_TKB.xls";
        String filename = (scheduleType != null && (scheduleType == 2 || scheduleType == 3))
                ? "lich_thi.xls"
                : "thoi_khoa_bieu.xls";

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        ClassPathResource resource = new ClassPathResource(templateName);
        try (InputStream is = resource.getInputStream();
                Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);

            if (scheduleType != null && (scheduleType == 2 || scheduleType == 3)) {
                // EXPORT LỊCH THI
                Row titleRow = sheet.getRow(4);
                if (titleRow != null) {
                    Cell cell = titleRow.getCell(0);
                    if (cell != null) {
                        String val = cell.getStringCellValue();
                        String[] years = (academicYear != null && academicYear.contains("-"))
                                ? academicYear.split("-")
                                : new String[] { "2025", "2026" };
                        val = val.replace("{nam đầu}", years[0])
                                .replace("{nam cuối}", years[1]);
                        cell.setCellValue(val);
                    }
                }

                Row templateRow = sheet.getRow(7);
                CellStyle[] styles = new CellStyle[12];
                if (templateRow != null) {
                    for (int i = 0; i < templateRow.getLastCellNum() && i < 12; i++) {
                        Cell c = templateRow.getCell(i);
                        if (c != null) {
                            styles[i] = c.getCellStyle();
                        }
                    }
                }

                int lastRowNum = sheet.getLastRowNum();
                for (int r = 8; r <= lastRowNum; r++) {
                    Row rowToClear = sheet.getRow(r);
                    if (rowToClear != null) {
                        sheet.removeRow(rowToClear);
                    }
                }

                int rowNum = 8;
                for (ScheduleEntity s : rows) {
                    Long courseId = null;
                    Long semId = null;
                    String courseCode = "";
                    String resolvedCourseName = "";

                    try {
                        Object[] csData = (Object[]) em.createNativeQuery("""
                                SELECT cs.course_id, cs.semester_id, c.course_code, c.course_name
                                FROM class_section cs
                                JOIN course c ON c.course_id = cs.course_id
                                WHERE cs.section_id = :sid LIMIT 1
                                """)
                                .setParameter("sid", s.getSectionId())
                                .getSingleResult();
                        courseId = ((Number) csData[0]).longValue();
                        semId = ((Number) csData[1]).longValue();
                        courseCode = (String) csData[2];
                        resolvedCourseName = (String) csData[3];
                    } catch (Exception ignored) {
                    }

                    List<Object[]> students = List.of();
                    if (courseId != null && semId != null) {
                        try {
                            students = em.createNativeQuery("""
                                    SELECT sp.student_code, up.full_name, sp.class_name
                                    FROM enrollment e
                                    JOIN student_profile sp ON sp.user_id = e.user_id
                                    JOIN user_profile up ON up.user_id = e.user_id
                                    WHERE e.course_id = :courseId
                                      AND e.semester_id = :semId
                                      AND e.status != 'đã hủy'
                                    ORDER BY sp.student_code
                                    """)
                                    .setParameter("courseId", courseId)
                                    .setParameter("semId", semId)
                                    .getResultList();
                        } catch (Exception ignored) {
                        }
                    }

                    int stt = 1;
                    for (Object[] std : students) {
                        Row r = sheet.createRow(rowNum++);
                        String studentCode = (String) std[0];
                        String fullName = (String) std[1];
                        String className = (String) std[2];

                        createCell(r, 0, stt++, styles[0]);
                        createCell(r, 1, studentCode, styles[1]);
                        createCell(r, 2, fullName, styles[2]);
                        createCell(r, 3, className, styles[3]);
                        createCell(r, 4, resolvedCourseName, styles[4]);
                        createCell(r, 5, courseCode, styles[5]);

                        String dateThiStr = "";
                        if (semesterStartDate != null && s.getWeekStart() != null && s.getDayOfWeek() != null) {
                            dateThiStr = semesterStartDate.plusWeeks(s.getWeekStart() - 1)
                                    .plusDays(s.getDayOfWeek() - 2).format(DATE_FMT);
                        }
                        createCell(r, 6, dateThiStr, styles[6]);
                        createCell(r, 7, s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : "", styles[7]);
                        createCell(r, 8, s.getEndTime() != null ? s.getEndTime().format(TIME_FMT) : "", styles[8]);
                        createCell(r, 9, s.getRoom() != null ? s.getRoom() : "", styles[9]);
                    }
                }
            } else {
                // EXPORT TKB
                Row titleRow = sheet.getRow(3);
                if (titleRow != null) {
                    Cell cell = titleRow.getCell(0);
                    if (cell != null) {
                        String val = cell.getStringCellValue();
                        String[] years = (academicYear != null && academicYear.contains("-"))
                                ? academicYear.split("-")
                                : new String[] { "2025", "2026" };
                        val = val.replace("{hoc_ky}", String.valueOf(semesterNumber))
                                .replace("{nam_dau}", years[0])
                                .replace("{nam_cuoi}", years[1]);
                        cell.setCellValue(val);
                    }
                }

                Row noteRow = sheet.getRow(5);
                if (noteRow != null) {
                    Cell cell = noteRow.getCell(0);
                    if (cell != null) {
                        String val = cell.getStringCellValue();
                        val = val.replace("{ghi_chu}", "");
                        cell.setCellValue(val);
                    }
                }

                Row templateRow = sheet.getRow(8);
                CellStyle[] styles = new CellStyle[15];
                if (templateRow != null) {
                    for (int i = 0; i < templateRow.getLastCellNum() && i < 15; i++) {
                        Cell c = templateRow.getCell(i);
                        if (c != null) {
                            styles[i] = c.getCellStyle();
                        }
                    }
                }

                int lastRowNum = sheet.getLastRowNum();
                for (int r = 8; r <= lastRowNum; r++) {
                    Row rowToClear = sheet.getRow(r);
                    if (rowToClear != null) {
                        sheet.removeRow(rowToClear);
                    }
                }

                int rowNum = 8;
                int stt = 1;
                for (ScheduleEntity s : rows) {
                    Row r = sheet.createRow(rowNum++);

                    String courseCode = "";
                    String resolvedCourseName = "";
                    Integer credits = 0;
                    String resolvedSectionCode = "";
                    Integer currentEnrollment = 0;

                    try {
                        Object[] csData = (Object[]) em.createNativeQuery("""
                                SELECT c.course_code, c.course_name, c.credits, cs.section_code, cs.current_enrollment
                                FROM class_section cs
                                JOIN course c ON c.course_id = cs.course_id
                                WHERE cs.section_id = :sid LIMIT 1
                                """)
                                .setParameter("sid", s.getSectionId())
                                .getSingleResult();
                        courseCode = (String) csData[0];
                        resolvedCourseName = (String) csData[1];
                        credits = ((Number) csData[2]).intValue();
                        resolvedSectionCode = (String) csData[3];
                        currentEnrollment = ((Number) csData[4]).intValue();
                    } catch (Exception ignored) {
                    }

                    createCell(r, 0, stt++, styles[0]);
                    createCell(r, 1, courseCode, styles[1]);
                    createCell(r, 2, credits, styles[2]);
                    createCell(r, 3, resolvedSectionCode, styles[3]);
                    createCell(r, 4, currentEnrollment, styles[4]);

                    String dowStr = "";
                    if (s.getDayOfWeek() != null) {
                        dowStr = s.getDayOfWeek() == 8 ? "CN" : String.valueOf(s.getDayOfWeek());
                    }
                    createCell(r, 5, dowStr, styles[5]);
                    createCell(r, 6, s.getStartPeriod() != null ? String.valueOf(s.getStartPeriod()) : "", styles[6]);

                    int numPeriods = 0;
                    if (s.getStartPeriod() != null && s.getEndPeriod() != null) {
                        numPeriods = s.getEndPeriod() - s.getStartPeriod() + 1;
                    }
                    createCell(r, 7, numPeriods > 0 ? String.valueOf(numPeriods) : "", styles[7]);
                    createCell(r, 8, s.getRoom() != null ? s.getRoom() : "", styles[8]);

                    String dateBdBstr = "";
                    if (semesterStartDate != null && s.getWeekStart() != null) {
                        dateBdBstr = semesterStartDate.plusWeeks(s.getWeekStart() - 1).format(DATE_FMT);
                    }
                    createCell(r, 9, dateBdBstr, styles[9]);

                    String dateKtStr = "";
                    if (semesterStartDate != null && s.getWeekEnd() != null) {
                        dateKtStr = semesterStartDate.plusWeeks(s.getWeekEnd() - 1).plusDays(6).format(DATE_FMT);
                    }
                    createCell(r, 10, dateKtStr, styles[10]);
                    createCell(r, 11, s.getLecturerName() != null ? s.getLecturerName() : "", styles[11]);
                    createCell(r, 12, s.getNotes() != null ? s.getNotes() : "", styles[12]);
                }
            }

            wb.write(response.getOutputStream());
        }
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
    // ════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ════════════════════════════════════════════════════════════════════════

    private ScheduleEntity findOrThrow(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch id=" + id));
    }

    /** Map entity → ScheduleResponse, join CLASS_SECTION + COURSE + SEMESTER */
    private ScheduleResponse toResponse(ScheduleEntity s) {
        ScheduleResponse.ScheduleResponseBuilder b = ScheduleResponse.builder()
                .scheduleId(s.getScheduleId())
                .userId(s.getUserId())
                .sectionId(s.getSectionId())
                .scheduleType(s.getScheduleType())
                .dayOfWeek(s.getDayOfWeek())
                .startPeriod(s.getStartPeriod())
                .endPeriod(s.getEndPeriod())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .room(s.getRoom())
                .building(s.getBuilding())
                .lecturerName(s.getLecturerName())
                .lecturerId(s.getLecturerId())
                .weekStart(s.getWeekStart())
                .weekEnd(s.getWeekEnd())
                .notes(s.getNotes())
                .updatedAt(s.getUpdatedAt());

        // Join CLASS_SECTION → COURSE + SEMESTER
        try {
            Object[] cs = (Object[]) em.createNativeQuery("""
                    SELECT cs.section_code, cs.course_id, cs.semester_id,
                           c.course_code, c.course_name,
                           sem.semester_name
                    FROM class_section cs
                    JOIN course c ON c.course_id = cs.course_id
                    JOIN semester sem ON sem.semester_id = cs.semester_id
                    WHERE cs.section_id = :sid
                    """)
                    .setParameter("sid", s.getSectionId())
                    .getSingleResult();

            b.sectionCode((String) cs[0])
                    .courseId(((Number) cs[1]).longValue())
                    .semesterId(((Number) cs[2]).longValue())
                    .courseCode((String) cs[3])
                    .courseName((String) cs[4])
                    .semesterName((String) cs[5]);
        } catch (Exception ignored) {
            /* section_id không hợp lệ — vẫn trả về phần còn lại */ }

        return b.build();
    }

    /** Map entity → ScheduleItemDto cho mobile */
    private ScheduleItemDto toItemDto(ScheduleEntity s) {
        String courseCode = "", courseName = "", type = "";
        try {
            Object[] c = (Object[]) em.createNativeQuery(
                    "SELECT course_code, course_name, theory_hours FROM course WHERE course_id = " +
                            "(SELECT course_id FROM class_section WHERE section_id = :sid)")
                    .setParameter("sid", s.getSectionId())
                    .getSingleResult();
            courseCode = (String) c[0];
            courseName = (String) c[1];
            type = ((Number) c[2]).intValue() > 0 ? "LÝ THUYẾT" : "THỰC HÀNH";
        } catch (Exception ignored) {
        }

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
                .startDate(getSemesterDate(s.getSectionId(), "start_date"))
                .endDate(getSemesterDate(s.getSectionId(), "end_date"))
                .weekStart(s.getWeekStart())
                .weekEnd(s.getWeekEnd())
                .room(s.getRoom())
                .building(s.getBuilding())
                .scheduleType(s.getScheduleType())
                .notes(s.getNotes())
                .build();
    }

    /** Lấy start_date / end_date từ SEMESTER qua CLASS_SECTION.section_id */
    private String getSemesterDate(Long sectionId, String column) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT sem." + column + " FROM semester sem " +
                            "JOIN class_section cs ON cs.semester_id = sem.semester_id " +
                            "WHERE cs.section_id = :sid")
                    .setParameter("sid", sectionId)
                    .getSingleResult();
            if (result instanceof java.sql.Date d)
                return d.toLocalDate().format(DATE_FMT);
            return result.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Tra lecturer_id từ full_name trong user_profile.
     * Trả về null nếu không tìm thấy (không ném exception).
     */
    private Long resolveLecturerId(String lecturerName) {
        if (lecturerName == null || lecturerName.isBlank())
            return null;
        try {
            Object r = em.createNativeQuery(
                    "SELECT u.user_id FROM user u " +
                            "JOIN user_profile up ON up.user_id = u.user_id " +
                            "WHERE up.full_name = :name LIMIT 1")
                    .setParameter("name", lecturerName.trim())
                    .getSingleResult();
            return ((Number) r).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Long resolveSectionId(String sectionCode) {
        try {
            Object r = em.createNativeQuery(
                    "SELECT section_id FROM class_section WHERE section_code = :code")
                    .setParameter("code", sectionCode)
                    .getSingleResult();
            return ((Number) r).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String cellStr(Row row, int col) {
        Cell c = row.getCell(col);
        return c == null ? null : c.toString().trim();
    }

    private Integer cellInt(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null)
            return null;
        try {
            return (int) c.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    private Long cellLong(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null)
            return null;
        try {
            return (long) c.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isBlank())
            return null;
        try {
            return LocalTime.parse(s, TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}