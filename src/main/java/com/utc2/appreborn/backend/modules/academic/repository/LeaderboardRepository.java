package com.utc2.appreborn.backend.modules.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utc2.appreborn.backend.modules.academic.entity.SemesterEntity;

import java.util.List;

public interface LeaderboardRepository extends JpaRepository<SemesterEntity, Long> {

    /**
     * Bảng xếp hạng theo một kỳ học cụ thể (lọc theo semester_id).
     * Dùng ROW_NUMBER() — yêu cầu MySQL 8.0+.
     */
    @Query(value = """
            SELECT ROW_NUMBER() OVER (ORDER BY s.gpa DESC) AS `rank`,
                   sp.student_code,
                   up.full_name,
                   s.gpa,
                   s.total_credits,
                   s.user_id
            FROM semester s
            JOIN student_profile sp ON sp.user_id = s.user_id
            JOIN user_profile    up ON up.user_id  = s.user_id
            WHERE s.semester_id = :semesterId
            ORDER BY s.gpa DESC
            """, nativeQuery = true)
    List<Object[]> findLeaderboardBySemester(@Param("semesterId") Long semesterId);

    /**
     * Bảng xếp hạng theo năm học (trung bình GPA các kỳ trong năm).
     */
    @Query(value = """
            SELECT ROW_NUMBER() OVER (ORDER BY AVG(s.gpa) DESC) AS `rank`,
                   sp.student_code,
                   up.full_name,
                   AVG(s.gpa)          AS gpa,
                   SUM(s.total_credits) AS total_credits,
                   s.user_id
            FROM semester s
            JOIN student_profile sp ON sp.user_id = s.user_id
            JOIN user_profile    up ON up.user_id  = s.user_id
            WHERE s.academic_year = :academicYear
            GROUP BY s.user_id, sp.student_code, up.full_name
            ORDER BY gpa DESC
            """, nativeQuery = true)
    List<Object[]> findLeaderboardByAcademicYear(@Param("academicYear") String academicYear);
}