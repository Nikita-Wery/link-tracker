CREATE OR REPLACE FUNCTION fill_chats()
RETURNS void
LANGUAGE sql
AS $$
    INSERT INTO chats (chat_id)
    SELECT generate_series(1, 1000);
$$;

SELECT fill_chats();
