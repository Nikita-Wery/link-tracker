package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import io.micrometer.core.instrument.Timer;
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
    private final ScrapperMetricsService scrapperMetricsService;

    @Transactional
    public void saveOutboxEvents(List<OutboxEvent> outboxEvents) {
        Timer.Sample sample = scrapperMetricsService.startRequestTimer();
        outboxEventRepository.addAll(outboxEvents);
        scrapperMetricsService.stopRequestTimer(sample, "database", "saveOutboxEvents");
    }

    @Transactional
    public List<OutboxEvent> findBatchPendingMessagesByTopic(int batchSize, String topic) {
        return scrapperMetricsService.timeExternalCall(
                "database",
                "findBatchPendingMessagesByTopic",
                "outboxeventservice",
                () -> outboxEventRepository.findBatchPendingMessagesAndSetProcessing(batchSize, topic));
    }

    @Transactional
    public void batchUpdateOutboxEventStatuses(List<OutboxEventUpdateDto> outboxEvents) {
        Timer.Sample sample = scrapperMetricsService.startRequestTimer();
        outboxEventRepository.updateMessageStatusBatch(outboxEvents);
        scrapperMetricsService.stopRequestTimer(sample, "database", "batchUpdateOutboxEventStatuses");
    }

    @Transactional
    public void markPendingEventsAsFailByTimeout(int timeout) {
        Timer.Sample sample = scrapperMetricsService.startRequestTimer();
        outboxEventRepository.markStuckPendingAsFail(timeout);
        scrapperMetricsService.stopRequestTimer(sample, "database", "markPendingEventsAsFailByTimeout");
    }
}
