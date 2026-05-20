-- V7__add_user_profile_address.sql
-- UserProfile entity có field `address` nhưng bảng user_profile chưa có cột này.

ALTER TABLE `user_profile`
    ADD COLUMN `address` VARCHAR(255) NULL
        COMMENT 'Địa chỉ thường trú';