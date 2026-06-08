package com.utc2.appreborn.backend.modules.schedule.repository;

import com.utc2.appreborn.backend.modules.schedule.entity.ScheduleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

        // ── MOBILE: tra lịch sinh viên ─────────────────────────────────────────

        @Query(value = """
                        SELECT DISTINCT s.* FROM schedule s
                        JOIN student_profile sp ON sp.student_code = :studentCode
                        LEFT JOIN enrollment e ON e.section_id = s.section_id AND e.user_id = sp.user_id AND e.status != 'đã hủy'
                        WHERE (s.user_id = sp.user_id)
                           OR (s.user_id IS NULL AND e.enrollment_id IS NOT NULL)
                        """, nativeQuery = true)
        List<ScheduleEntity> findByStudentCode(@Param("studentCode") String studentCode);

        @Query(value = """
                        SELECT MAX(s.updated_at) FROM schedule s
                        JOIN student_profile sp ON sp.student_code = :studentCode
                        LEFT JOIN enrollment e ON e.section_id = s.section_id AND e.user_id = sp.user_id AND e.status != 'đã hủy'
                        WHERE (s.user_id = sp.user_id)
                           OR (s.user_id IS NULL AND e.enrollment_id IS NOT NULL)
                        """, nativeQuery = true)
        Optional<LocalDateTime> findLastUpdatedByStudentCode(@Param("studentCode") String studentCode);

        // ── ADMIN WEB: tra theo lớp học phần ──────────────────────────────────

        @Query(value = """
                        SELECT s.* FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        WHERE (:sectionId   IS NULL OR s.section_id      = :sectionId)
                          AND (:semesterId  IS NULL OR cs.semester_id    = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type   = :scheduleType)
                          AND (:keyword     IS NULL
                               OR cs.section_code  LIKE CONCAT('%', :keyword, '%')
                               OR s.room           LIKE CONCAT('%', :keyword, '%'))
                        ORDER BY s.day_of_week, s.start_period
                        """, countQuery = """
                        SELECT COUNT(*) FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        WHERE (:sectionId   IS NULL OR s.section_id      = :sectionId)
                          AND (:semesterId  IS NULL OR cs.semester_id    = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type   = :scheduleType)
                          AND (:keyword     IS NULL
                               OR cs.section_code  LIKE CONCAT('%', :keyword, '%')
                               OR s.room           LIKE CONCAT('%', :keyword, '%'))
                        """, nativeQuery = true)
        Page<ScheduleEntity> findBySection(
                        @Param("sectionId") Long sectionId,
                        @Param("semesterId") Long semesterId,
                        @Param("scheduleType") Integer scheduleType,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        // ── ADMIN WEB: tra theo giảng viên ────────────────────────────────────

        @Query(value = """
                        SELECT s.* FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        WHERE (:lecturerId   IS NULL OR s.lecturer_id              = :lecturerId)
                          AND (:lecturerName IS NULL
                               OR s.lecturer_name LIKE CONCAT('%', :lecturerName, '%'))
                          AND (:semesterId   IS NULL OR cs.semester_id             = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type            = :scheduleType)
                        ORDER BY s.day_of_week, s.start_period
                        """, countQuery = """
                        SELECT COUNT(*) FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        WHERE (:lecturerId   IS NULL OR s.lecturer_id              = :lecturerId)
                          AND (:lecturerName IS NULL
                               OR s.lecturer_name LIKE CONCAT('%', :lecturerName, '%'))
                          AND (:semesterId   IS NULL OR cs.semester_id             = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type            = :scheduleType)
                        """, nativeQuery = true)
        Page<ScheduleEntity> findByLecturer(
                        @Param("lecturerId") Long lecturerId,
                        @Param("lecturerName") String lecturerName,
                        @Param("semesterId") Long semesterId,
                        @Param("scheduleType") Integer scheduleType,
                        Pageable pageable);

        // ── ADMIN WEB: toàn bộ lịch (phân trang)

        @Query(value = """
                        SELECT s.* FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        JOIN course c ON c.course_id = cs.course_id
                        WHERE (:semesterId   IS NULL OR cs.semester_id   = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type  = :scheduleType)
                          AND (:lecturerId   IS NULL OR s.lecturer_id    = :lecturerId)
                          AND (:sectionCode  IS NULL OR cs.section_code  LIKE CONCAT('%', :sectionCode, '%'))
                          AND (:room         IS NULL OR s.room           LIKE CONCAT('%', :room, '%'))
                          AND (:lecturerName IS NULL OR s.lecturer_name  LIKE CONCAT('%', :lecturerName, '%'))
                          AND (:dayOfWeek    IS NULL OR s.day_of_week    = :dayOfWeek)
                          AND (:period       IS NULL OR s.start_period   = :period)
                          AND (:courseName   IS NULL OR c.course_name    LIKE CONCAT('%', :courseName, '%'))
                        ORDER BY cs.semester_id, s.day_of_week, s.start_period
                        """, countQuery = """
                        SELECT COUNT(*) FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        JOIN course c ON c.course_id = cs.course_id
                        WHERE (:semesterId   IS NULL OR cs.semester_id   = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type  = :scheduleType)
                          AND (:lecturerId   IS NULL OR s.lecturer_id    = :lecturerId)
                          AND (:sectionCode  IS NULL OR cs.section_code  LIKE CONCAT('%', :sectionCode, '%'))
                          AND (:room         IS NULL OR s.room           LIKE CONCAT('%', :room, '%'))
                          AND (:lecturerName IS NULL OR s.lecturer_name  LIKE CONCAT('%', :lecturerName, '%'))
                          AND (:dayOfWeek    IS NULL OR s.day_of_week    = :dayOfWeek)
                          AND (:period       IS NULL OR s.start_period   = :period)
                          AND (:courseName   IS NULL OR c.course_name    LIKE CONCAT('%', :courseName, '%'))
                        """, nativeQuery = true)
        Page<ScheduleEntity> findAllPaged(
                        @Param("semesterId") Long semesterId,
                        @Param("scheduleType") Integer scheduleType,
                        @Param("lecturerId") Long lecturerId,
                        @Param("sectionCode") String sectionCode,
                        @Param("courseName") String courseName,
                        @Param("dayOfWeek") Integer dayOfWeek,
                        @Param("period") Integer period,
                        @Param("room") String room,
                        @Param("lecturerName") String lecturerName,
                        Pageable pageable);
        // ── UPSERT lookup ─────────────────────────────────────────────────────

        @Query(value = """
                        SELECT s.* FROM schedule s
                        WHERE s.section_id    = :sectionId
                          AND s.schedule_type = :scheduleType
                          AND s.day_of_week   = :dayOfWeek
                          AND s.start_period  = :startPeriod
                        LIMIT 1
                        """, nativeQuery = true)
        Optional<ScheduleEntity> findForUpsert(
                        @Param("sectionId") Long sectionId,
                        @Param("scheduleType") Integer scheduleType,
                        @Param("dayOfWeek") Integer dayOfWeek,
                        @Param("startPeriod") Integer startPeriod);

        // ── EXPORT ────────────────────────────────────────────────────────────

        @Query(value = """
                        SELECT s.* FROM schedule s
                        JOIN class_section cs ON cs.section_id = s.section_id
                        JOIN course c ON c.course_id = cs.course_id
                        WHERE (:semesterId   IS NULL OR cs.semester_id   = :semesterId)
                          AND (:scheduleType IS NULL OR s.schedule_type  = :scheduleType)
                          AND (:lecturerId   IS NULL OR s.lecturer_id    = :lecturerId)
                          AND (:room         IS NULL OR s.room           LIKE CONCAT('%', :room, '%'))
                          AND (:sectionCode  IS NULL OR cs.section_code  LIKE CONCAT('%', :sectionCode, '%'))
                          AND (:courseName   IS NULL OR c.course_name    LIKE CONCAT('%', :courseName, '%'))
                          AND (:dayOfWeek    IS NULL OR s.day_of_week    = :dayOfWeek)
                          AND (:weekStart    IS NULL OR s.week_start     >= :weekStart)
                          AND (:weekEnd      IS NULL OR s.week_end       <= :weekEnd)
                        ORDER BY cs.semester_id, s.day_of_week, s.start_period
                        """, nativeQuery = true)
        List<ScheduleEntity> findForExport(
                        @Param("semesterId") Long semesterId,
                        @Param("scheduleType") Integer scheduleType,
                        @Param("lecturerId") Long lecturerId,
                        @Param("room") String room,
                        @Param("sectionCode") String sectionCode,
                        @Param("courseName") String courseName,
                        @Param("dayOfWeek") Integer dayOfWeek,
                        @Param("weekStart") Integer weekStart,
                        @Param("weekEnd") Integer weekEnd);
}
