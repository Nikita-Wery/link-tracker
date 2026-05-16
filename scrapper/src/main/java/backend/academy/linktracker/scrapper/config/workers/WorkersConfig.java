package backend.academy.linktracker.scrapper.config.workers;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.properties.LinkUpdateWorkerProperties;
import backend.academy.linktracker.scrapper.properties.OutboxEventUpdateWorkerProperties;
import backend.academy.linktracker.scrapper.service.BatchWorker;
import backend.academy.linktracker.scrapper.service.LinkUpdateProcessor;
import backend.academy.linktracker.scrapper.service.OutboxEventService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class WorkersConfig {

    @Bean
    public BatchWorker<LinkUpdate> linkUpdateWorker(
            @Qualifier("linkUpdateWorkerExecutor") ThreadPoolTaskExecutor executor,
            LinkUpdateWorkerProperties properties,
            LinkUpdateProcessor processor) {

        return new BatchWorker<>(executor, properties, processor::processBatch);
    }

    @Bean
    @ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public BatchWorker<OutboxEventUpdateDto> outboxEventWorker(
            @Qualifier("linkUpdateWorkerExecutor") ThreadPoolTaskExecutor executor,
            OutboxEventUpdateWorkerProperties properties,
            OutboxEventService service) {

        return new BatchWorker<>(executor, properties, service::batchUpdateOutboxEventStatuses);
    }
}
