package com.utc2.appreborn.backend.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.utc2.appreborn.backend.common.enums.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    @Size(max = 100)
    private String fullName;

    // NOTE: field tên là "phone" (không phải "phoneNumber").
    // Client phải gửi JSON key "phone", không phải "phoneNumber".
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @Size(max = 255)
    private String address;

    // FIX BUG 1: Nhận String "yyyy-MM-dd" từ JSON rồi parse sang LocalDate.
    // Tránh lỗi 400 khi client gửi String thay vì LocalDate object.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;
    private String avatarUrl;
}