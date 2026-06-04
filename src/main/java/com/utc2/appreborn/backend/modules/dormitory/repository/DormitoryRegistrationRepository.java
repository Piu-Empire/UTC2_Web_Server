package com.utc2.appreborn.backend.modules.dormitory.repository;

import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DormitoryRegistrationRepository extends JpaRepository<DormitoryRegistrationEntity, Long> {

    /**
     * Lấy lịch sử đăng ký KTX của sinh viên kèm thông tin phòng.
     * Column order:
     * 0=dorm_reg_id, 1=user_id, 2=room_id, 3=registered_at,
     * 4=start_date,  5=end_date, 6=status, 7=total_fee, 8=paid_status,
     * 9=room_code,   10=building, 11=room_type, 12=price_per_month
     */
    @Query(value = """
            SELECT dr.dorm_reg_id, dr.user_id, dr.room_id, dr.registered_at,
                   dr.start_date, dr.end_date, dr.status, dr.total_fee, dr.paid_status,
                   r.room_code, r.building, r.room_type, r.price_per_month
            FROM dormitory_registration dr
            JOIN dormitory_room r ON r.room_id = dr.room_id
            WHERE dr.user_id = :userId
            ORDER BY dr.registered_at DESC
            """, nativeQuery = true)
    List<Object[]> findRegistrationsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndStatusNot(Long userId, String status);

    /**
     * Lấy toàn bộ đăng ký KTX của TẤT CẢ sinh viên kèm thông tin profile — dùng cho export Excel.
     * Column order:
     * 0=student_code, 1=full_name, 2=class_name, 3=faculty,
     * 4=room_code, 5=building, 6=room_type,
     * 7=start_date, 8=end_date, 9=status, 10=total_fee, 11=paid_status, 12=registered_at
     */
    @Query(value = """
            SELECT sp.student_code, up.full_name, sp.class_name, sp.faculty,
                   r.room_code, r.building, r.room_type,
                   dr.start_date, dr.end_date, dr.status, dr.total_fee, dr.paid_status, dr.registered_at
            FROM dormitory_registration dr
            JOIN dormitory_room r ON r.room_id = dr.room_id
            JOIN student_profile sp ON sp.user_id = dr.user_id
            JOIN user_profile up ON up.user_id = dr.user_id
            ORDER BY r.building ASC, r.room_code ASC, sp.student_code ASC
            """, nativeQuery = true)
    List<Object[]> findAllDormRegistrationsForExport();
}