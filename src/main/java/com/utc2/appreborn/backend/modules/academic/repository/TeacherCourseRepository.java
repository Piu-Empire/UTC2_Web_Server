package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.TeacherCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherCourseRepository extends JpaRepository<TeacherCourseEntity, Long> {

    List<TeacherCourseEntity> findByUserId(Long userId);

    /**
     * Check giáo viên có được phân công dạy courseId + className không.
     * className NULL trong DB = dạy tất cả lớp → cũng pass.
     */
    @Query(value = """
            SELECT COUNT(*) > 0
            FROM teacher_course
            WHERE user_id   = :userId
              AND course_id = :courseId
              AND (class_name = :className OR class_name IS NULL)
            """, nativeQuery = true)
    boolean isAssigned(@Param("userId")    Long   userId,
                       @Param("courseId")  Long   courseId,
                       @Param("className") String className);

    /**
     * Check enrollmentId có thuộc môn giáo viên được phân công không.
     */
    @Query(value = """
            SELECT COUNT(*) > 0
            FROM teacher_course tc
            JOIN enrollment e ON e.course_id = tc.course_id
            JOIN student_profile sp ON sp.user_id = e.user_id
            WHERE tc.user_id        = :userId
              AND e.enrollment_id   = :enrollmentId
              AND (tc.class_name    = sp.class_name OR tc.class_name IS NULL)
            """, nativeQuery = true)
    boolean canUpdateEnrollment(@Param("userId")       Long userId,
                                @Param("enrollmentId") Long enrollmentId);
}