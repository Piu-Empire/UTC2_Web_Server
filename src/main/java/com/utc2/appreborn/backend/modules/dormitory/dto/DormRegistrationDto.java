package com.utc2.appreborn.backend.modules.dormitory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DormRegistrationDto {
    private Long   dormRegId;
    private Long   roomId;
    private String roomCode;
    private String building;
    private String roomType;
    private Double pricePerMonth;
    private String startDate;
    private String endDate;
    private String status;
    private Double totalFee;
    private String paidStatus;
    private String registeredAt;
    // Thông tin sinh viên — dùng cho admin view
    private String studentName;
    private String studentCode;
    private String email;
    private String className;

    // Alias cho frontend — frontend dùng item.id ở mọi chỗ
    public Long getId() {
        return dormRegId;
    }
}