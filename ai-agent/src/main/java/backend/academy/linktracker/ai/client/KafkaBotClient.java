package backend.academy.linktracker.ai.client;

import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.properties.ProcessedLinkUpdatesTopicProperties;
import backend.academy.linktracker.ai.utils.mappers.AvroMapper;
import backend.academy.linktracker.contract.avro.ProcessedLinkUpdateEvent;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@Slf4j
@RequiredArgsConstructor
public class KafkaBotClient {

    private final ProcessedLinkUpdatesTopicProperties props;
    private final KafkaTemplate<Long, ProcessedLinkUpdateEvent> kafkaAvroTemplate;
    private final AvroMapper avroMapper;

    public CompletableFuture<SendResult<Long, ProcessedLinkUpdateEvent>> send(
            Long key, ProcessedLinkUpdate processedLinkUpdate) {

        log.info("Sending processedLinkUpdate with id {}", processedLinkUpdate.id());

        ProcessedLinkUpdateEvent event = avroMapper.processedLinkUpdateToEvent(processedLinkUpdate);

        return kafkaAvroTemplate.send(props.getName(), key, event).whenComplete((r, t) -> {
            if (t != null) {
                log.error(
                        "Failed to send raw link update, eventKey {}, updateId {}",
                        r.getProducerRecord().key(),
                        processedLinkUpdate.id(),
                        t);
            } else {
                log.info(
                        "Send raw link update to processed event - successful, eventKey {}, updateId {}",
                        r.getProducerRecord().key(),
                        processedLinkUpdate.id());
            }
        });
    }
}
