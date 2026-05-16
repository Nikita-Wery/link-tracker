package backend.academy.linktracker.scrapper.config.kafka;

import backend.academy.linktracker.scrapper.client.inner.kafka.KafkaBotClient;
import backend.academy.linktracker.scrapper.client.inner.kafka.OutboxEventSender;
import backend.academy.linktracker.scrapper.client.inner.kafka.impl.JsonOutboxEventSender;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import backend.academy.linktracker.scrapper.service.BatchWorker;
import backend.academy.linktracker.scrapper.utils.JsonToEntityDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "json", matchIfMissing = true)
public class JsonConfig {

    @Bean
    public OutboxEventSender<LinkUpdate> outboxEventSender(
            KafkaTemplate<Long, LinkUpdate> kafkaTemplate,
            JsonToEntityDeserializer deserializer,
            LinkUpdateTopicProperties properties) {
        return new JsonOutboxEventSender(kafkaTemplate, deserializer, properties);
    }

    @Bean
    public KafkaBotClient<LinkUpdate> kafkaBotClient(
            OutboxEventSender<LinkUpdate> outboxEventSender, BatchWorker<OutboxEventUpdateDto> batchWorker) {
        return new KafkaBotClient(outboxEventSender, batchWorker);
    }
}
