package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ДИСКЛЕЙМЕР
 * Этот класс существует только по тому, что по тз нужно разделять
 * jdbc и orm, но каждый знает, что оптимальнее всего - использовать
 * orm + jdbc в солжных кейсах и так как я не могу некоторые методы
 * занести в jdbcRepo(который создаётся в jdbc конфигурации), я сделаю
 * это здесь
 */
@Component
@RequiredArgsConstructor
public class JdbcHelper {

    private final JdbcTemplate jdbcTemplate;

    // language=sql
    private static final String UPDATE_LINK_LAST_UPD_BATCH = """
        UPDATE links SET latest_update_time = ? WHERE link_id = ?
        """;

    // language=sql
    private static final String BATCH_UPDATE_MESSAGE_STATUSES = """
            UPDATE outbox_event
            SET status = ?
            WHERE id = ?
        """;

    public void updateLastUpdateBatch(List<LinkUpdate> batch, int batchSize) {

        jdbcTemplate.batchUpdate(UPDATE_LINK_LAST_UPD_BATCH, batch, batchSize, (ps, linkUpdate) -> {
            ps.setObject(1, linkUpdate.getLastUpdate());
            ps.setLong(2, linkUpdate.id());
        });
    }

    public void updateMessageStatusBatch(List<OutboxEventUpdateDto> outboxEventUpdateDtos) {
        jdbcTemplate.batchUpdate(
                BATCH_UPDATE_MESSAGE_STATUSES,
                outboxEventUpdateDtos,
                outboxEventUpdateDtos.size(),
                (ps, outboxEvent) -> {
                    ps.setString(1, outboxEvent.futureMessageStatuses().name());
                    ps.setLong(2, outboxEvent.outboxEventId());
                });
    }
}
