CREATE TABLE chat_action_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100),
    action_type VARCHAR(100) NOT null,
    action_label VARCHAR(255),
    action_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
