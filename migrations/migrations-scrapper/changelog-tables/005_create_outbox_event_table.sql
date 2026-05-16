CREATE TABLE outbox_event (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_event_seq'),
    key BIGINT NOT NULL,
    topic VARCHAR(255) NOT NULL,
    event_body TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    status VARCHAR(50)
);
