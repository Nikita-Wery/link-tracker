package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;

@AllArgsConstructor
public class JdbcChatRepository implements ChatRepository {

    // language=sql
    private static final String INSERT_CHAT = "INSERT INTO chats (chat_id) VALUES (:chatId)";

    // language=sql
    private static final String DELETE_CHAT_BY_ID = "DELETE FROM chats WHERE chat_id = :chatId";

    // language=sql
    private static final String SELECT_CHAT_BY_ID = "SELECT chat_id FROM chats WHERE chat_id = :chatId";

    // language=sql
    private static final String EXISTS_BY_ID = "SELECT EXISTS (SELECT 1 FROM chats WHERE chat_id = :chatId)";

    private final JdbcClient jdbcClient;

    @Override
    public boolean existsById(long chatId) {
        return jdbcClient
                .sql(EXISTS_BY_ID)
                .param("chatId", chatId)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Chat save(Chat chat) {
        jdbcClient.sql(INSERT_CHAT).param("chatId", chat.getChatId()).update();

        return chat;
    }

    @Override
    public Chat saveAndFlush(Chat chat) {
        return save(chat);
    }

    @Override
    public int deleteByChatId(long chatId) {
        return jdbcClient.sql(DELETE_CHAT_BY_ID).param("chatId", chatId).update();
    }

    @Override
    public Optional<Chat> findChatByChatId(long chatId) {
        return jdbcClient
                .sql(SELECT_CHAT_BY_ID)
                .param("chatId", chatId)
                .query((rs, rowNum) -> new Chat(rs.getLong("chat_id")))
                .optional();
    }
}
