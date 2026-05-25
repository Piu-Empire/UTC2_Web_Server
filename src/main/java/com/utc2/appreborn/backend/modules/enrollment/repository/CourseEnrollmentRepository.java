package com.utc2.appreborn.backend.modules.enrollment.repository;

import com.utc2.appreborn.backend.modules.enrollment.entity.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseEnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    /**
     * Lấy toàn bộ enrollment của sinh viên kèm thông tin course và semester.
     * Column order:
     * 0=enrollment_id, 1=course_code, 2=course_name, 3=credits,
     * 4=semester_name, 5=status, 6=midterm_score, 7=final_score,
     * 8=assignment_score, 9=total_score, 10=letter_grade, 11=grade_point,
     * 12=is_passed, 13=registered_at
     */
    @Query(value = """
            SELECT e.enrollment_id, c.course_code, c.course_name, c.credits,
                   s.semester_name, e.status, e.midterm_score, e.final_score,
                   e.assignment_score, e.total_score, e.letter_grade, e.grade_point,
                   e.is_passed, e.registered_at
            FROM enrollment e
            JOIN course c ON c.course_id = e.course_id
            JOIN semester s ON s.semester_id = e.semester_id
            WHERE e.user_id = :userId
            ORDER BY e.registered_at DESC
            """, nativeQuery = true)
    List<Object[]> findEnrollmentsByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra sinh viên đã đăng ký môn này chưa (status != 'đã hủy').
     */
    boolean existsByUserIdAndCourseIdAndStatusNot(Long userId, Long courseId, String status);

    /**
     * Tính tổng tín chỉ đã đăng ký trong học kỳ (status != 'đã hủy').
     */
    @Query(value = """
            SELECT COALESCE(SUM(c.credits), 0)
            FROM enrollment e
            JOIN course c ON c.course_id = e.course_id
            WHERE e.user_id = :userId
            AND e.semester_id = :semesterId
            AND e.status != 'đã hủy'
            """, nativeQuery = true)
    Integer sumCreditsByUserIdAndSemesterId(@Param("userId") Long userId,
                                            @Param("semesterId") Long semesterId);
}
