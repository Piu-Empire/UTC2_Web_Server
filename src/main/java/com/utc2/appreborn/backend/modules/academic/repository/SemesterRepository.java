package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.SemesterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemesterRepository extends JpaRepository<SemesterEntity, Long> {

    // user là ManyToOne relation → dùng user_Id để traverse
    List<SemesterEntity> findByUser_IdOrderBySemesterNumberAsc(Long userId);

    List<SemesterEntity> findByAcademicYear(String academicYear);
}
