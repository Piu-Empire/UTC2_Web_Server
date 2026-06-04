package com.utc2.appreborn.backend.modules.finance.repository;

import com.utc2.appreborn.backend.modules.finance.entity.TuitionRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TuitionRateRepository extends JpaRepository<TuitionRate, Long> {

    /**
     * Lấy giá tín chỉ theo năm học — nếu không có năm học cụ thể thì dùng bản ghi NULL (mặc định).
     * Ưu tiên: khớp academicYear trước, fallback về NULL.
     */
    @Query(value = """
            SELECT * FROM tuition_rate
            WHERE academic_year = :academicYear OR academic_year IS NULL
            ORDER BY academic_year DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<TuitionRate> findByAcademicYearOrDefault(@Param("academicYear") String academicYear);

    /** Lấy giá mặc định (academic_year IS NULL) */
    Optional<TuitionRate> findFirstByAcademicYearIsNullOrderByEffectiveFromDesc();
}