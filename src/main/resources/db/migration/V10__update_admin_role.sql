-- =========================================================
-- V10: Cấp quyền ADMIN + đặt password thật cho 2211020001
-- Password plain text: 123456
-- =========================================================

UPDATE `user`
SET
    `role`          = 'ADMIN',
    `enabled`       = true,
    `password_hash` = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi'
WHERE `email` = '2211020001@st.utc2.edu.vn';