package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcHelper;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaOutboxEventRepository;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrmOutboxEventRepository implements OutboxEventRepository {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final JdbcHelper jdbcHelper;

    @Override
    public void addAll(List<OutboxEvent> outboxEvents) {
        jpaOutboxEventRepository.saveAll(outboxEvents);
    }

    @Override
    public List<OutboxEvent> findBatchPendingMessagesAndSetProcessing(int batchSize, String topic) {
        return jpaOutboxEventRepository.findBatchPendingMessagesAndChangeStatus(batchSize, topic);
    }

    @Override
    public void updateMessageStatusBatch(List<OutboxEventUpdateDto> outboxEventUpdateDtos) {
        jdbcHelper.updateMessageStatusBatch(outboxEventUpdateDtos);
    }

    @Override
    public void markStuckPendingAsFail(int minutes) {}
}
