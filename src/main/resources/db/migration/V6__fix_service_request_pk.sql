-- V6__fix_service_request_pk.sql
ALTER TABLE `service_request`
    CHANGE COLUMN `request_id` `id` BIGINT NOT NULL AUTO_INCREMENT;