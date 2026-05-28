package backend.academy.linktracker.scrapper.client.inner.kafka;

import backend.academy.linktracker.scrapper.domain.MessageStatus;
import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.service.BatchWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaClient<T> {

    private final OutboxEventSender<T> sender;
    private final BatchWorker<OutboxEventUpdateDto> batchWorker;

    public KafkaClient(OutboxEventSender<T> sender, BatchWorker<OutboxEventUpdateDto> batchWorker) {
        this.sender = sender;
        this.batchWorker = batchWorker;
    }

    public void sendOutboxEventTypeLinkUpdate(OutboxEvent outboxEvent) {

        try {

            T event = sender.deserialize(outboxEvent.getEventBody());

            sender.send(outboxEvent.getKey(), event).whenComplete((sendResult, t) -> {
                if (t != null) {

                    log.atError()
                            .setCause(t)
                            .addKeyValue("link_update_id", sender.extractEventId(event))
                            .addKeyValue("link_update_url", sender.extractUrl(event))
                            .addKeyValue("exception_message", t.getMessage())
                            .log("Error while sending linkUpdate");

                    batchWorker.submit(new OutboxEventUpdateDto(outboxEvent.getId(), MessageStatus.FAILED));

                    return;
                }

                batchWorker.submit(new OutboxEventUpdateDto(outboxEvent.getId(), MessageStatus.SENT));
            });

            log.info("Outbox event {}, with key {}, was sent", outboxEvent.getId(), outboxEvent.getKey());

        } catch (Exception e) {

            log.error("Failed to send LinkUpdateEvent, outbox_event_id={}", outboxEvent.getId(), e);

            batchWorker.submit(new OutboxEventUpdateDto(outboxEvent.getId(), MessageStatus.FAILED));
        }
    }
}
