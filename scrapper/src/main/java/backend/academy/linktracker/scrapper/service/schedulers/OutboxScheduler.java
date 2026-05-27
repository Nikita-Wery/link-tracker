package backend.academy.linktracker.scrapper.service.schedulers;

import backend.academy.linktracker.scrapper.client.inner.kafka.KafkaBotClient;
import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.properties.OutboxSchedulerProperties;
import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import backend.academy.linktracker.scrapper.service.OutboxEventService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxScheduler {

    private final OutboxEventService outboxEventService;
    private final KafkaBotClient<?> kafkaBotClient;
    private final OutboxSchedulerProperties outboxSchedulerProperties;
    private final LinkUpdateTopicProperties linkUpdateTopicProperties;

    @Scheduled(fixedDelayString = "${app.scheduler.outbox.interval-update-ms}")
    public void publishLinkUpdateEvents() {
        log.info("OutboxScheduler starts task");

        List<OutboxEvent> outboxEvents = outboxEventService.findBatchPendingMessagesByTopic(
                outboxSchedulerProperties.getBatchSize(), linkUpdateTopicProperties.getName());

        for (OutboxEvent event : outboxEvents) {
            log.info("Outbox event {} preparing for shipment", event.getId());

            kafkaBotClient.sendOutboxEventTypeLinkUpdate(event);
        }
    }
}
