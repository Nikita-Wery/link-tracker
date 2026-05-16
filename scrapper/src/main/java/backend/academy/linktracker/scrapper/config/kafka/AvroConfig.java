package backend.academy.linktracker.scrapper.config.kafka;

import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.client.inner.kafka.KafkaBotClient;
import backend.academy.linktracker.scrapper.client.inner.kafka.OutboxEventSender;
import backend.academy.linktracker.scrapper.client.inner.kafka.impl.AvroOutboxEventSender;
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
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro", matchIfMissing = true)
public class AvroConfig {

    @Bean
    public OutboxEventSender<LinkUpdateEvent> outboxEventSender(
            KafkaTemplate<Long, LinkUpdateEvent> kafkaTemplate,
            JsonToEntityDeserializer deserializer,
            LinkUpdateTopicProperties properties) {
        return new AvroOutboxEventSender(kafkaTemplate, deserializer, properties);
    }

    @Bean
    public KafkaBotClient<LinkUpdateEvent> kafkaBotClient(
            OutboxEventSender<LinkUpdateEvent> outboxEventSender, BatchWorker<OutboxEventUpdateDto> batchWorker) {
        return new KafkaBotClient(outboxEventSender, batchWorker);
    }
}
