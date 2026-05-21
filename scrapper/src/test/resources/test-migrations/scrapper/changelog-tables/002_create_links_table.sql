CREATE TABLE links (
    link_id BIGINT PRIMARY KEY DEFAULT nextval('link_sequence'),
    url TEXT NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    latest_update_time TIMESTAMP WITH TIME ZONE DEFAULT now(),

    CONSTRAINT unq_link_url UNIQUE (url)
);
