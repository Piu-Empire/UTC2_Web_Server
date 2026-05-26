package com.utc2.appreborn.backend.modules.dormitory.repository;

import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DormitoryRoomRepository extends JpaRepository<DormitoryRoomEntity, Long> {

    /**
     * Tìm phòng theo mã phòng — dùng cho import (check duplicate).
     */
    Optional<DormitoryRoomEntity> findByRoomCode(String roomCode);

    /**
     * Lấy toàn bộ phòng KTX, sắp xếp theo tòa rồi mã phòng.
     */
    @Query(value = """
            SELECT room_id, room_code, building, floor,
                   capacity, current_occupancy, room_type,
                   price_per_month, status, amenities
            FROM dormitory_room
            ORDER BY building ASC, room_code ASC
            """, nativeQuery = true)
    List<Object[]> findAllRoomsOrdered();
}