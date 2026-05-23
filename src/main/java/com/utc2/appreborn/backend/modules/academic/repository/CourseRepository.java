package com.utc2.appreborn.backend.modules.academic.repository;

import com.utc2.appreborn.backend.modules.academic.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    Optional<CourseEntity> findByCourseCode(String courseCode);
}