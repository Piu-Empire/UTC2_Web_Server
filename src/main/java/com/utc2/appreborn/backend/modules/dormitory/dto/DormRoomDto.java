package com.utc2.appreborn.backend.modules.dormitory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DormRoomDto {
    private Long    roomId;
    private String  roomCode;
    private String  building;
    private Integer floor;
    private Integer capacity;
    private Integer currentOccupancy;
    private String  roomType;
    private Double  pricePerMonth;
    private String  status;
    private String  amenities;
    private Boolean available;
}
