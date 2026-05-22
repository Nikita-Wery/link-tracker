CREATE OR REPLACE FUNCTION fill_chat_link_tags()
RETURNS void
LANGUAGE sql
AS $$
    INSERT INTO chat_link_tags (chat_link_id, tag)
    SELECT
        cl.chat_link_id,
        substr(md5(random()::text || gs::text || cl.chat_link_id::text), 1, 12)
    FROM chat_link cl
    CROSS JOIN generate_series(1, 5) gs;
$$;

SELECT fill_chat_link_tags();
