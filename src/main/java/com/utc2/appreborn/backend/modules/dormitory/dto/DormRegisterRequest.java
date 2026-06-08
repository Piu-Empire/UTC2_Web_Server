package com.utc2.appreborn.backend.modules.dormitory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DormRegisterRequest {

    @NotNull(message = "roomId không được để trống")
    private Long roomId;

    @NotNull(message = "Số tháng không được để trống")
    @Min(value = 1, message = "Số tháng phải ít nhất là 1")
    private Integer months;
}
