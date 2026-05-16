package com.utc2.appreborn.backend.modules.schedule.repository;

import com.utc2.appreborn.backend.modules.schedule.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    // Join qua student_profile để tìm theo studentCode (MSSV)
    @Query(value = """
            SELECT s.* FROM schedule s
            JOIN student_profile sp ON sp.user_id = s.user_id
            WHERE sp.student_code = :studentCode
            """, nativeQuery = true)
    List<ScheduleEntity> findByStudentCode(@Param("studentCode") String studentCode);

    @Query(value = """
            SELECT MAX(s.updated_at) FROM schedule s
            JOIN student_profile sp ON sp.user_id = s.user_id
            WHERE sp.student_code = :studentCode
            """, nativeQuery = true)
    Optional<LocalDateTime> findLastUpdatedByStudentCode(@Param("studentCode") String studentCode);
}
