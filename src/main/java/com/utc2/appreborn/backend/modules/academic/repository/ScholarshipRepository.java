package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.ScholarshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScholarshipRepository extends JpaRepository<ScholarshipEntity, Long> {

    /** App: chỉ lấy học bổng đã duyệt (pending_status IN approved, received) */
    @Query(value = """
            SELECT s.scholarship_id, s.name, s.organization, s.amount, s.unit,
                   s.min_gpa, s.description,
                   ss.pending_status, ss.received_at, ss.approved_at
            FROM scholarship s
            LEFT JOIN student_scholarship ss
                ON ss.scholarship_id = s.scholarship_id AND ss.user_id = :userId
            WHERE ss.pending_status IN ('approved','received')
            ORDER BY s.scholarship_id ASC
            """, nativeQuery = true)
    List<Object[]> findApprovedByUserId(@Param("userId") Long userId);

    /** Admin/advisor: lấy tất cả kể cả pending */
    @Query(value = """
            SELECT s.scholarship_id, s.name, s.organization, s.amount, s.unit,
                   s.min_gpa, s.description,
                   ss.pending_status, ss.received_at, ss.approved_at
            FROM scholarship s
            LEFT JOIN student_scholarship ss
                ON ss.scholarship_id = s.scholarship_id AND ss.user_id = :userId
            ORDER BY s.scholarship_id ASC
            """, nativeQuery = true)
    List<Object[]> findAllWithStatusByUserId(@Param("userId") Long userId);

    /** Pending list cho lv5 duyệt */
    @Query(value = """
            SELECT ss.user_id, up.full_name, sp.student_code,
                   s.scholarship_id, s.name, ss.pending_status, ss.received_at
            FROM student_scholarship ss
            JOIN scholarship s   ON s.scholarship_id = ss.scholarship_id
            JOIN user_profile up ON up.user_id = ss.user_id
            JOIN student_profile sp ON sp.user_id = ss.user_id
            WHERE ss.pending_status IN ('pending', 'approved', 'not_received')
            ORDER BY ss.pending_status ASC, ss.user_id, s.scholarship_id
            """, nativeQuery = true)
    List<Object[]> findPendingApprovals();
}