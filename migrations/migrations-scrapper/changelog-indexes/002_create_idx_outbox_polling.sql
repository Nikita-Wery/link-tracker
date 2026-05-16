CREATE INDEX idx_outbox_polling
ON outbox_event(status, topic, created_at);
