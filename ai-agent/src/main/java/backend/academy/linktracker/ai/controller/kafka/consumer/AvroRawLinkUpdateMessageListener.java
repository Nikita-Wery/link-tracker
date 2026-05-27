package backend.academy.linktracker.ai.controller.kafka.consumer;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.service.RawLinkUpdatesProcessor;
import backend.academy.linktracker.ai.utils.mappers.AvroMapper;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;

@Slf4j
@Component
// TODO: возможно тут этого не нужно так как у меня только avro
@ConditionalOnProperty(name = "app.client.scrapper.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
@RequiredArgsConstructor
public class AvroRawLinkUpdateMessageListener {

    // TODO: добавить
    private final AvroMapper avroMapper;
    private final RawLinkUpdatesProcessor processor;

    // TODO: chanhe topic name
    @KafkaListener(containerFactory = "avroConsumerFactory", topics = "${app.kafka.topics.link-update}")
    @RetryableTopic(
        backOff = @BackOff(delay = 1000L, multiplier = 2.0),
        attempts = "3",
        autoCreateTopics = "true",
        kafkaTemplate = "dlqAvroLinkUpdateKafkaTemplate",
        topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
        include = RuntimeException.class)
    public void consume(ConsumerRecord<Long, RawLinkUpdateEvent> record, Acknowledgment ack) {

        log.info("A message with key: {} was received", record.key());

        RawLinkUpdate linkUpdate = avroMapper.rawLinkUpdateEventToRawLinkUpdate(record.value());

        processor.processRawLinkUpdate(record.key(), record.topic(), linkUpdate, ack);
    }
}
