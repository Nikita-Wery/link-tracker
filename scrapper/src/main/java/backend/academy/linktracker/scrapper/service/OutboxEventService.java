package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void saveOutboxEvents(List<OutboxEvent> outboxEvents) {
        outboxEventRepository.addAll(outboxEvents);
    }

    @Transactional
    public List<OutboxEvent> findBatchPendingMessagesByTopic(int batchSize, String topic) {
        return outboxEventRepository.findBatchPendingMessagesAndSetProcessing(batchSize, topic);
    }

    @Transactional
    public void batchUpdateOutboxEventStatuses(List<OutboxEventUpdateDto> outboxEvents) {
        outboxEventRepository.updateMessageStatusBatch(outboxEvents);
    }
}
