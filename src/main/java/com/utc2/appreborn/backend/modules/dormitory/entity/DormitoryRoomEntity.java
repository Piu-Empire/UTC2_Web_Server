package com.utc2.appreborn.backend.modules.dormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dormitory_room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DormitoryRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_code")
    private String roomCode;

    @Column(name = "building")
    private String building;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "current_occupancy")
    private Integer currentOccupancy;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "price_per_month", columnDefinition = "DECIMAL(15,2)")
    private Double pricePerMonth;

    @Column(name = "status")
    private String status;

    @Column(name = "amenities")
    private String amenities;
}
