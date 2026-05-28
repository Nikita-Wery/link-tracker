package backend.academy.linktracker.bot.controller.kafka.consumer.linkupdate;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.utils.mappers.AvroMapper;
import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.client.scrapper.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
@RequiredArgsConstructor
public class AvroLinkUpdateMessageListener {

    private final AvroMapper avroMapper;
    private final LinkUpdateMessageProcessor processor;

    @KafkaListener(containerFactory = "avroConsumerFactory", topics = "${app.kafka.topics.link-processed-updates}")
    @RetryableTopic(
            backOff = @BackOff(delay = 1000L, multiplier = 2.0),
            attempts = "3",
            autoCreateTopics = "true",
            kafkaTemplate = "dlqAvroLinkUpdateKafkaTemplate",
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            include = RuntimeException.class)
    public void consume(ConsumerRecord<Long, LinkUpdateEvent> record, Acknowledgment ack) {

        log.info("A message with key: {} was received", record.key());

        LinkUpdate linkUpdate = avroMapper.mappLinkUpdateEventToLinkUpdate(record.value());

        processor.process(record.key(), record.topic(), linkUpdate, ack);
    }
}
