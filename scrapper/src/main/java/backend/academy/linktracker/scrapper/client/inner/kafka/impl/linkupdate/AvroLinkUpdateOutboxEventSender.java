package backend.academy.linktracker.scrapper.client.inner.kafka.impl.linkupdate;

import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.client.inner.kafka.OutboxEventSender;
import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import backend.academy.linktracker.scrapper.utils.JsonToEntityDeserializer;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
@RequiredArgsConstructor
public class AvroLinkUpdateOutboxEventSender implements OutboxEventSender<LinkUpdateEvent> {

    private final KafkaTemplate<Long, LinkUpdateEvent> kafkaTemplate;
    private final JsonToEntityDeserializer deserializer;
    private final LinkUpdateTopicProperties properties;

    @Override
    public LinkUpdateEvent deserialize(String json) {
        return deserializer.deserializeLinkUpdateEvent(json);
    }

    @Override
    public Long extractEventId(LinkUpdateEvent event) {
        return event.getId();
    }

    @Override
    public String extractUrl(LinkUpdateEvent event) {
        return event.getUrl().toString();
    }

    @Override
    public CompletableFuture<SendResult<Long, LinkUpdateEvent>> send(Long key, LinkUpdateEvent event) {

        return kafkaTemplate.send(properties.getName(), key, event);
    }
}
