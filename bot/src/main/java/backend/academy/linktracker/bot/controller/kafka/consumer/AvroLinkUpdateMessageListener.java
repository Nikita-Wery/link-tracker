package backend.academy.linktracker.bot.controller.kafka.consumer;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.bot.utils.mappers.AvroMapper;
import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.retrytopic.TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdateMessageListener {

    private final LinkUpdateService linkUpdateService;
    private final AvroMapper avroMapper;

    // TODO: подумать как обработать ошибку десериализации
    @KafkaListener(containerFactory = "avroLinkUpdateConsumerFactory", topics = "${app.link-updates.avro-topic}")
    @RetryableTopic(
        backOff = @BackOff(delay = 1000L, multiplier = 2.0),
        attempts = "3",
        autoCreateTopics = "true",
        kafkaTemplate = "dlqLinkUpdateKafkaTemplate",
        topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
        include = RuntimeException.class
    )
    public void consumeAvroLinkUpdate(ConsumerRecord<Long, LinkUpdateEvent> record, Acknowledgment ack) {

        LinkUpdate linkUpdate = avroMapper.mappLinkUpdateEventToLinkUpdate(record.value());

        log.info("Received event from topic: {}, linkUpdate id {}, updated url {}",
            record.topic(),
            linkUpdate.id(),
            linkUpdate.url()
        );

        linkUpdateService.sendUpdateMessage(linkUpdate);
        ack.acknowledge();
    }

}
