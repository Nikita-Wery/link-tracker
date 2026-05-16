package backend.academy.linktracker.scrapper.client.inner.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.scrapper.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "json", matchIfMissing = true)
public class JsonLinkUpdateMessageListener {

    private final LinkUpdateService linkUpdateService;
    private final ProcessedMessagesService processedMessagesService;

    // TODO: подумать как обработать ошибку десериализации
    @KafkaListener(containerFactory = "jsonConsumerFactory", topics = "${app.kafka.link-updates.json-topic}")
    @RetryableTopic(
        backOff = @BackOff(delay = 1000L, multiplier = 2.0),
        attempts = "3",
        autoCreateTopics = "true",
        kafkaTemplate = "dlqJsonLinkUpdateKafkaTemplate",
        topicSuffixingStrategy = SUFFIX_WITH_INDEX_VALUE,
        include = RuntimeException.class
    )
    public void consumeAvroLinkUpdate(ConsumerRecord<Long, LinkUpdate> record, Acknowledgment ack) {

        LinkUpdate linkUpdate = record.value();

        log.info("Received event from topic: {}, linkUpdate id {}, updated url {}",
            record.topic(),
            linkUpdate.id(),
            linkUpdate.url()
        );

        Optional<ProcessedMessage> processedMessageOpt = processedMessagesService.findProcessedMessageById(linkUpdate.id());

        if (processedMessageOpt.isPresent()) {

            log.warn("Found already processed message with id {}, the message will not be sent.",
                linkUpdate.id(),
                kv("link_url", linkUpdate.url()));

            return;
        }

        linkUpdateService.sendUpdateMessage(linkUpdate);

        processedMessagesService.save(new ProcessedMessage(record.key()));

        ack.acknowledge();
    }

}
