package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.ScholarshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScholarshipRepository extends JpaRepository<ScholarshipEntity, Long> {

    /**
     * Lấy tất cả học bổng kèm trạng thái của sinh viên.
     * Column order:
     * 0=scholarship_id, 1=name, 2=organization, 3=amount, 4=unit,
     * 5=min_gpa, 6=description, 7=status, 8=received_at
     */
    @Query(value = """
            SELECT s.scholarship_id, s.name, s.organization, s.amount, s.unit,
                   s.min_gpa, s.description, ss.status, ss.received_at
            FROM scholarship s
            LEFT JOIN student_scholarship ss ON ss.scholarship_id = s.scholarship_id
                AND ss.user_id = :userId
            ORDER BY s.scholarship_id ASC
            """, nativeQuery = true)
    List<Object[]> findAllWithStatusByUserId(@Param("userId") Long userId);
}
