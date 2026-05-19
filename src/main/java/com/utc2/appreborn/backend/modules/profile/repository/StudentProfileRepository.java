package com.utc2.appreborn.backend.modules.profile.repository;

import com.utc2.appreborn.backend.modules.profile.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByStudentCode(String studentCode);

    Optional<StudentProfile> findByUserId(Long userId);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user WHERE sp.studentCode = :studentCode")
    Optional<StudentProfile> findByStudentCodeWithUser(@Param("studentCode") String studentCode);
}