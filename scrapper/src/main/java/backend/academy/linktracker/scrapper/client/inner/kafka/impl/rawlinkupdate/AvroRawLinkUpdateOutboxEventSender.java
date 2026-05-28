package backend.academy.linktracker.scrapper.client.inner.kafka.impl.rawlinkupdate;

import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import backend.academy.linktracker.scrapper.client.inner.kafka.OutboxEventSender;
import backend.academy.linktracker.scrapper.properties.topics.RawLinkUpdateTopicProperties;
import backend.academy.linktracker.scrapper.utils.JsonToEntityDeserializer;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
@RequiredArgsConstructor
public class AvroRawLinkUpdateOutboxEventSender implements OutboxEventSender<RawLinkUpdateEvent> {

    private final KafkaTemplate<Long, RawLinkUpdateEvent> kafkaTemplate;
    private final JsonToEntityDeserializer deserializer;
    private final RawLinkUpdateTopicProperties properties;

    @Override
    public RawLinkUpdateEvent deserialize(String json) {
        return deserializer.deserializeRawLinkUpdateEvent(json);
    }

    @Override
    public Long extractEventId(RawLinkUpdateEvent event) {
        return event.getId();
    }

    @Override
    public String extractUrl(RawLinkUpdateEvent event) {
        return event.getUrl().toString();
    }

    @Override
    public CompletableFuture<SendResult<Long, RawLinkUpdateEvent>> send(Long key, RawLinkUpdateEvent event) {

        return kafkaTemplate.send(properties.getName(), key, event);
    }
}
