package backend.academy.linktracker.scrapper.client.inner.kafka.impl;

import backend.academy.linktracker.scrapper.client.inner.kafka.OutboxEventSender;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import backend.academy.linktracker.scrapper.utils.JsonToEntityDeserializer;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Slf4j
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "json")
@RequiredArgsConstructor
public class JsonOutboxEventSender implements OutboxEventSender<LinkUpdate> {

    private final KafkaTemplate<Long, LinkUpdate> kafkaTemplate;
    private final JsonToEntityDeserializer deserializer;
    private final LinkUpdateTopicProperties properties;

    @Override
    public LinkUpdate deserialize(String json) {
        return deserializer.deserializeLinkUpdate(json);
    }

    @Override
    public Long extractEventId(LinkUpdate event) {
        return event.id();
    }

    @Override
    public String extractUrl(LinkUpdate event) {
        return event.url().toString();
    }

    @Override
    public CompletableFuture<SendResult<Long, LinkUpdate>> send(Long key, LinkUpdate event) {

        log.info("SEND topic={}, key={}", properties.getName(), key);

        return kafkaTemplate.send(properties.getName(), key, event);
    }
}
