package com.utc2.appreborn.backend.modules.profile.repository;

import com.utc2.appreborn.backend.modules.profile.entity.StudentProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
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

    @Query(
        value = """
            SELECT sp 
            FROM StudentProfileEntity sp 
            LEFT JOIN UserProfileEntity up ON sp.userId = up.userId 
            WHERE (:search IS NULL 
                OR LOWER(up.fullName) LIKE LOWER(CONCAT('%', :search, '%')) 
                OR LOWER(sp.studentCode) LIKE LOWER(CONCAT('%', :search, '%'))) 
              AND (:faculty IS NULL OR LOWER(sp.faculty) = LOWER(:faculty)) 
              AND (:cohort IS NULL OR LOWER(sp.academicYear) = LOWER(:cohort)) 
              AND (:status IS NULL OR LOWER(sp.status) = LOWER(:status))
            """,
        countQuery = """
            SELECT COUNT(sp) 
            FROM StudentProfileEntity sp 
            LEFT JOIN UserProfileEntity up ON sp.userId = up.userId 
            WHERE (:search IS NULL 
                OR LOWER(up.fullName) LIKE LOWER(CONCAT('%', :search, '%')) 
                OR LOWER(sp.studentCode) LIKE LOWER(CONCAT('%', :search, '%'))) 
              AND (:faculty IS NULL OR LOWER(sp.faculty) = LOWER(:faculty)) 
              AND (:cohort IS NULL OR LOWER(sp.academicYear) = LOWER(:cohort)) 
              AND (:status IS NULL OR LOWER(sp.status) = LOWER(:status))
            """
    )
    Page<StudentProfileEntity> searchStudents(
            @Param("search") String search,
            @Param("faculty") String faculty,
            @Param("cohort") String cohort,
            @Param("status") String status,
            Pageable pageable);
}