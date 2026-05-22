CREATE OR REPLACE FUNCTION fill_chat_link()
RETURNS void
LANGUAGE sql
AS $$
    INSERT INTO chat_link (chat_id, link_id)
    SELECT
        c.chat_id,
        l.link_id
    FROM chats c
    CROSS JOIN LATERAL (
        SELECT link_id
        FROM links
        ORDER BY random()
        LIMIT 100
    ) l;
$$;

SELECT fill_chat_link();
