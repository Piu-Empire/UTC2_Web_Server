package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.ScholarshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScholarshipRepository extends JpaRepository<ScholarshipEntity, Long> {

    /**
     * Trả về tất cả học bổng kèm trạng thái của sinh viên hiện tại.
     * LEFT JOIN để hiển thị cả học bổng chưa có bản ghi trong student_scholarship.
     * status = NULL nghĩa là sinh viên chưa được xét.
     */
    @Query(value = """
            SELECT sch.scholarship_id, sch.name, sch.organization,
                   sch.amount, sch.unit, sch.min_gpa, sch.description,
                   ss.status, ss.received_at
            FROM scholarship sch
            LEFT JOIN student_scholarship ss
                ON ss.scholarship_id = sch.scholarship_id AND ss.user_id = :userId
            ORDER BY sch.scholarship_id ASC
            """, nativeQuery = true)
    List<Object[]> findAllWithStatusByUserId(@Param("userId") Long userId);
}