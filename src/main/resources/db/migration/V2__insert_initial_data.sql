-- Chèn tài khoản Admin
INSERT INTO users (username, email, password, role, enabled, created_at)
VALUES ('admin', 'admin@utc2.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1, NOW());
