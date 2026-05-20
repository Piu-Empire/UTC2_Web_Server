package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    /**
     * Lấy toàn bộ enrollment của một sinh viên, kèm thông tin kỳ học.
     * Dùng cho màn hình Xem điểm (filter phía service bằng semesterId nếu cần).
     */
    @Query(value = """
            SELECT e.*, c.course_code, c.course_name, c.credits,
                   s.semester_name, s.academic_year
            FROM enrollment e
            JOIN course c ON c.course_id = e.course_id
            JOIN semester s ON s.semester_id = e.semester_id
            WHERE e.user_id = :userId
            ORDER BY e.semester_id DESC, c.course_code ASC
            """, nativeQuery = true)
    List<Object[]> findGradesByUserId(@Param("userId") Long userId);

    /**
     * Lấy enrollment của một sinh viên trong một kỳ học cụ thể.
     */
    @Query(value = """
            SELECT e.*, c.course_code, c.course_name, c.credits,
                   s.semester_name, s.academic_year
            FROM enrollment e
            JOIN course c ON c.course_id = e.course_id
            JOIN semester s ON s.semester_id = e.semester_id
            WHERE e.user_id = :userId AND e.semester_id = :semesterId
            ORDER BY c.course_code ASC
            """, nativeQuery = true)
    List<Object[]> findGradesByUserIdAndSemesterId(@Param("userId") Long userId,
                                                    @Param("semesterId") Long semesterId);
}