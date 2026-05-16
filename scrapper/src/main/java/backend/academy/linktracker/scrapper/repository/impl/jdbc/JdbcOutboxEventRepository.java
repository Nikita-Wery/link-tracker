package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JdbcOutboxEventRepository implements OutboxEventRepository {

    // language=sql имба
    private static final String SELECT_BATCH_OUTBOX_EVENTS = """
        WITH selected AS (
            SELECT id
            FROM outbox_event
            WHERE status = 'PENDING'
              AND topic = :topic
            ORDER BY created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        )
        UPDATE outbox_event oe
        SET status = 'PROCESSING',
            updated_at = CURRENT_TIMESTAMP
        FROM selected s
        WHERE oe.id = s.id
        RETURNING oe.*;
    """;

    // language=sql
    private static final String INSERT_BATCH_OUTBOX_EVENTS =
            "INSERT INTO outbox_event (key, topic, event_body, status, updated_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;
    private final JdbcHelper jdbcHelper;

    @Override
    public void addAll(List<OutboxEvent> outboxEvents) {
        jdbcTemplate.batchUpdate(INSERT_BATCH_OUTBOX_EVENTS, outboxEvents, outboxEvents.size(), (ps, outboxEvent) -> {
            ps.setLong(1, outboxEvent.getKey());
            ps.setString(2, outboxEvent.getTopic());
            ps.setString(3, outboxEvent.getEventBody());
            ps.setString(4, outboxEvent.getMessageStatus().name());
        });
    }

    @Override
    @Transactional
    public List<OutboxEvent> findBatchPendingMessagesAndSetProcessing(int batchSize, String topic) {
        return jdbcClient
                .sql(SELECT_BATCH_OUTBOX_EVENTS)
                .param("batchSize", batchSize)
                .param("topic", topic)
                .query(OutboxEvent.class)
                .list();
    }

    @Override
    public void updateMessageStatusBatch(List<OutboxEventUpdateDto> outboxEventUpdateDtos) {
        jdbcHelper.updateMessageStatusBatch(outboxEventUpdateDtos);
    }
}
