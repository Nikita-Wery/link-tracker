CREATE TABLE chat_link (
    chat_link_id BIGINT PRIMARY KEY DEFAULT nextval('chat_link_sequence'),
    chat_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,

    CONSTRAINT fk_chat_link_chat
        FOREIGN KEY (chat_id)
        REFERENCES chats(chat_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_chat_link_link
        FOREIGN KEY (link_id)
        REFERENCES links(link_id)
        ON DELETE CASCADE,

    CONSTRAINT unq_chatid_linkurl
        UNIQUE (chat_id, link_id)
);
CREATE INDEX idx_chat_link_chat_id
    ON chat_link(chat_id);
