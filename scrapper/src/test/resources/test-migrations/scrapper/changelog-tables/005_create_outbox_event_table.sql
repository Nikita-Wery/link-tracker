CREATE TABLE outbox_event (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_event_seq'),
    key BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    event_body JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    status VARCHAR(50)
);
CREATE INDEX idx_outbox_polling
    ON outbox_event(status, topic, created_at);
