package backend.academy.linktracker.ai.controller.kafka.consumer;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.service.RawLinkUpdatesProcessor;
import backend.academy.linktracker.ai.utils.mappers.AvroMapper;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvroRawLinkUpdateMessageListener {

    private final AvroMapper avroMapper;
    private final RawLinkUpdatesProcessor processor;

    @KafkaListener(containerFactory = "avroConsumerFactory", topics = "${app.kafka.topics.link-raw-updates.name}")
    @RetryableTopic(
            backOff = @BackOff(delay = 1000L, multiplier = 2.0),
            attempts = "${app.kafka.topics.link-raw-updates.attempts}",
            autoCreateTopics = "true",
            kafkaTemplate = "dlqAvroRawLinkUpdateKafkaTemplate",
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            include = RuntimeException.class)
    public void consume(ConsumerRecord<Long, RawLinkUpdateEvent> record, Acknowledgment ack) {

        log.info("A message with key: {} was received", record.key());

        RawLinkUpdate linkUpdate = avroMapper.rawLinkUpdateEventToRawLinkUpdate(record.value());

        processor.processRawLinkUpdate(record.key(), record.topic(), linkUpdate, ack);
    }
}
