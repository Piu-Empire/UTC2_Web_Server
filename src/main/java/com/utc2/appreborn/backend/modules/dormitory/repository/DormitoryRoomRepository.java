package com.utc2.appreborn.backend.modules.dormitory.repository;

import com.utc2.appreborn.backend.modules.dormitory.entity.DormitoryRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DormitoryRoomRepository extends JpaRepository<DormitoryRoomEntity, Long> {

    /**
     * Lấy toàn bộ phòng KTX, sắp xếp theo tòa rồi mã phòng.
     * Column order:
     * 0=room_id, 1=room_code, 2=building, 3=floor, 4=capacity,
     * 5=current_occupancy, 6=room_type, 7=price_per_month, 8=status, 9=amenities
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
