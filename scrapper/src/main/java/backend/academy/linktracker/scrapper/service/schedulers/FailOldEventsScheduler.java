package backend.academy.linktracker.scrapper.service.schedulers;

import backend.academy.linktracker.scrapper.properties.FailOldEventsSchedulerProperties;
import backend.academy.linktracker.scrapper.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class FailOldEventsScheduler {

    private final OutboxEventService outboxEventService;
    private final FailOldEventsSchedulerProperties properties;

    @Scheduled(fixedDelayString = "${app.scheduler.link-update.interval-update-ms}")
    public void checkLinks() {
        outboxEventService.markPendingEventsAsFailByTimeout(properties.getIntervalTimeoutMin());
    }
}
