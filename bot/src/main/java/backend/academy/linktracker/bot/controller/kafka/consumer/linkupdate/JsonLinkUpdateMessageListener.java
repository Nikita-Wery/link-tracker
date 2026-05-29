package backend.academy.linktracker.bot.controller.kafka.consumer.linkupdate;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;

import backend.academy.linktracker.bot.dto.LinkUpdate;
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
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "json", matchIfMissing = true)
@RequiredArgsConstructor
public class JsonLinkUpdateMessageListener {

    private final LinkUpdateMessageProcessor processor;

    @KafkaListener(containerFactory = "jsonConsumerFactory", topics = "${app.kafka.topics.link-updates.name}")
    @RetryableTopic(
            backOff = @BackOff(delay = 1000L, multiplier = 2.0),
            attempts = "${app.kafka.topics.link-updates.attempts}",
            autoCreateTopics = "true",
            kafkaTemplate = "dlqJsonLinkUpdateKafkaTemplate",
            topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
            include = RuntimeException.class)
    public void consume(ConsumerRecord<Long, LinkUpdate> record, Acknowledgment ack) {

        log.info("A message with key: {} was received", record.key());

        processor.process(record.key(), record.topic(), record.value(), ack);
    }
}
