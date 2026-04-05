package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Repository
@Slf4j
@AllArgsConstructor
public class JdbcChatLinkRepository implements ChatLinkRepository {

    // language=sql
    private static final String INSERT_CHATLINK
        = "INSERT INTO chat_link (chat_link_id, chat_id, link_url) VALUES (:chatLinkId, :chatId, :linkUrl)";

    // language=sql
    private static final String EXISTS_BY_ID
        = "SELECT EXISTS (SELECT 1 FROM chat_link WHERE chat_link_id = :chatLinkId)";

    // language=sql
    private static final String SELECT_CHATLINKS_BY_CHATID
        = "SELECT chat_link_id, chat_id, link_url FROM chat_link WHERE chat_id = :chatId";

    // language=sql
    private static final String INSERT_TAGS
        = "INSERT INTO chat_link_tags (chat_link_id, tag) VALUES (?, ?)";

    // language=sql
    private static final String SELECT_FULL_CHATLINKS_BY_CHATID =
        """
        SELECT cl.chat_link_id, cl.chat_id, cl.link_url, l.link_id, l.resource_type, l.latest_update_time, t.tag
        FROM chat_link cl
        JOIN links l ON cl.link_url = l.url
        JOIN chats c ON cl.chat_id = c.chat_id
        LEFT JOIN chat_link_tags t ON cl.chat_link_id = t.chat_link_id
        WHERE cl.chat_id = :chatId;
        """;

    // language=sql
    private static final String DELETE_BY_CHATID_URL_RETURNING_CHATLINK =
        """
        WITH deleted_cl AS (
            DELETE FROM chat_link
            WHERE chat_id = :chatId AND link_url = :linkUrl
            RETURNING chat_link_id
        ),
        deleted_tags AS (
            DELETE FROM chat_link_tags
            WHERE chat_link_id IN (SELECT chat_link_id FROM deleted_cl)
            RETURNING chat_link_id, tag
        )
        SELECT
            d.chat_link_id,
            t.tag
        FROM deleted_cl d
        LEFT JOIN deleted_tags t ON d.chat_link_id = t.chat_link_id;
        """;

    // language=sql
    private static final String SELECT_NEXT_ID
        = "SELECT nextval('CHAT_LINK_SEQUENCE')";

    private final JdbcClient jdbcClient;
    private final DataSource dataSource;

    @Override
    public ChatLink save(ChatLink chatLink) {
        Long chatLinkId = jdbcClient.sql(SELECT_NEXT_ID)
            .query(Long.class)
            .single();

        jdbcClient.sql(INSERT_CHATLINK)
            .param("chatLinkId", chatLinkId)
            .param("chatId", chatLink.getChat().getChatId())
            .param("linkUrl", chatLink.getLink().getUrl())
            .update();

        chatLink.setChatLinkId(chatLinkId);

        if (chatLink.getTags() != null && !chatLink.getTags().isEmpty()) {
            addTagsToChatLink(chatLinkId, chatLink.getTags());
        }

        return chatLink;
    }

    @Override
    public boolean existsById(long linkId) {
        return jdbcClient.sql(EXISTS_BY_ID)
            .param("chatLinkId", linkId)
            .query(Boolean.class)
            .single();
    }

    @Override
    public List<ChatLink> findChatLinksByChatId(long chatId) {
        Map<Long, ChatLink> map = new LinkedHashMap<>();

        jdbcClient.sql(SELECT_FULL_CHATLINKS_BY_CHATID)
            .param("chatId", chatId)
            .query(rs -> {
                while (rs.next()) {

                    Long chatLinkId = rs.getLong("chat_link_id");

                    ChatLink chatLink = map.get(chatLinkId);

                    if (chatLink == null) {

                        Link link = new Link(
                            rs.getString("link_url"),
                            ResourceType.valueOf(rs.getString("resource_type")),
                            rs.getObject("latest_update_time", OffsetDateTime.class)
                        );

                        link.setLinkId(rs.getLong("link_id"));

                        Chat chat = new Chat(rs.getLong("chat_id"));

                        chatLink = new ChatLink(link, chat);
                        chatLink.setChatLinkId(chatLinkId);

                        map.put(chatLinkId, chatLink);
                    }

                    String tag = rs.getString("tag");
                    if (tag != null) {
                        chatLink.getTags().add(tag);
                    }
                }
            });

        return new ArrayList<>(map.values());
    }

    @Override
    public Optional<ChatLink> deleteChatLinkReturningChatLink(ChatLink chatLink) {

        Optional<ChatLink> result = jdbcClient.sql(DELETE_BY_CHATID_URL_RETURNING_CHATLINK)
            .param("chatId", chatLink.getChat().getChatId())
            .param("linkUrl", chatLink.getLink().getUrl())
            .query(rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Long id = rs.getLong("chat_link_id");
                chatLink.setChatLinkId(id);

                do {
                    String tag = rs.getString("tag");
                    if (tag != null) {
                        chatLink.getTags().add(tag);
                    }
                } while (rs.next());

                return Optional.of(chatLink);
            });

        return result;
    }

    @SuppressFBWarnings(
        value = "SLF4J_PLACE_HOLDER_MISMATCH",
        justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void addTagsToChatLink(long chatLinkId, Set<String> tags) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource);
             PreparedStatement ps = connection.prepareStatement(INSERT_TAGS)) {

            for (String tag : tags) {
                ps.setLong(1, chatLinkId);
                ps.setString(2, tag);
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException ex) {
            log.error("SQL error when adding tags",
                kv("insert_query", INSERT_TAGS)
            );

            throw new ScrapperApiException(ex.getMessage(), "Error when adding tags");
        }

    }
}
