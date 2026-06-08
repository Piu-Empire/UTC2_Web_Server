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
     * 12=is_passed, 13=registered_at, 14=semester_number, 15=academic_year
     */
    @Query(value = """
            SELECT e.enrollment_id, c.course_code, c.course_name, c.credits,
                   s.semester_name, e.status, e.midterm_score, e.final_score,
                   e.assignment_score, e.total_score, e.letter_grade, e.grade_point,
                   e.is_passed, e.registered_at, s.semester_number, s.academic_year
            FROM enrollment e
            JOIN course c ON c.course_id = e.course_id
            JOIN semester s ON s.semester_id = e.semester_id
            WHERE e.user_id = :userId
            ORDER BY s.academic_year ASC, s.semester_number ASC, c.course_code ASC
            """, nativeQuery = true)
    List<Object[]> findEnrollmentsByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra sinh viên đã đăng ký môn này chưa (status != 'đã hủy').
     */
    boolean existsByUserIdAndCourseIdAndStatusNot(Long userId, Long courseId, String status);

    /**
     * Lấy toàn bộ đăng ký học phần của TẤT CẢ sinh viên kèm thông tin profile — dùng cho export Excel.
     * Column order:
     * 0=student_code, 1=full_name, 2=class_name, 3=faculty,
     * 4=course_code, 5=course_name, 6=credits,
     * 7=semester_name, 8=academic_year, 9=status, 10=registered_at
     */
    @Query(value = """
            SELECT sp.student_code, up.full_name, sp.class_name, sp.faculty,
                   c.course_code, c.course_name, c.credits,
                   s.semester_name, s.academic_year, e.status, e.registered_at
            FROM enrollment e
            JOIN course c ON c.course_id = e.course_id
            JOIN semester s ON s.semester_id = e.semester_id
            JOIN student_profile sp ON sp.user_id = e.user_id
            JOIN user_profile up ON up.user_id = e.user_id
            ORDER BY s.academic_year ASC, s.semester_number ASC, sp.student_code ASC, c.course_code ASC
            """, nativeQuery = true)
    List<Object[]> findAllEnrollmentsForExport();

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