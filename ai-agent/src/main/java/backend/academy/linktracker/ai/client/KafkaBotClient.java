package backend.academy.linktracker.ai.client;

import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.properties.RawLinkUpdatesTopicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class KafkaBotClient {

    private final RawLinkUpdatesTopicProperties props;
    // TODO: добавить LinkProcessedUpdate
    private final KafkaTemplate<Long, ProcessedLinkUpdate> kafkaAvroTemplate;

    public KafkaBotClient(RawLinkUpdatesTopicProperties props, KafkaTemplate<Long, ProcessedLinkUpdate> kafkaTemplate) {
        this.props = props;
        this.kafkaAvroTemplate = kafkaTemplate;
    }

    // TODO: подумать над результатом
    public CompletableFuture<SendResult<Long, ProcessedLinkUpdate>> send(Long key, ProcessedLinkUpdate event) {

        return kafkaAvroTemplate.send(props.getName(), key, event);
    }


}
