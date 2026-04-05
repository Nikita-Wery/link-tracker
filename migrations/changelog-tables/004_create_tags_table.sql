CREATE TABLE chat_link_tags (
    chat_link_id BIGINT NOT NULL,
    tag TEXT NOT NULL,

    PRIMARY KEY (chat_link_id, tag),

    FOREIGN KEY (chat_link_id)
        REFERENCES chat_link(chat_link_id)
        ON DELETE CASCADE
);
