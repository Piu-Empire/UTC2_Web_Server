package com.utc2.appreborn.backend.modules.profile.repository;

import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfileEntity, Long> {
    Optional<StudentProfileEntity> findByStudentCode(String studentCode);

    Optional<StudentProfileEntity> findByUserId(Long userId);

    @Query("""
            SELECT sp
            FROM StudentProfileEntity sp
            JOIN FETCH sp.user
            WHERE sp.studentCode = :studentCode
            """)
    Optional<StudentProfileEntity> findByStudentCodeWithUser(
            @Param("studentCode") String studentCode);
}