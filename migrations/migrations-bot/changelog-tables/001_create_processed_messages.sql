CREATE TABLE processed_messages (
    message_id BIGINT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
