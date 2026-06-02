-- =========================================================
-- V10: Cấp quyền ADMIN + đặt password thật cho 2211020001
-- Password plain text: 123456789
-- =========================================================

UPDATE `user`
SET
    `role`          = 'ADMIN',
    `enabled`       = true,
    `password_hash` = '$2a$12$1r6iM2aHetFoGWmajB1EBegjLTRrjUjyB.SBD85TR/T51hYdDw9CC'
WHERE `email` = '2211020001@st.utc2.edu.vn';
