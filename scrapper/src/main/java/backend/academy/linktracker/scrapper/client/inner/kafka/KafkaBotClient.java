package backend.academy.linktracker.scrapper.client.inner.kafka;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.MessageStatus;
import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.OutboxEventUpdateDto;
import backend.academy.linktracker.scrapper.service.BatchWorker;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaBotClient<T> {

    private final OutboxEventSender<T> sender;
    private final BatchWorker<OutboxEventUpdateDto> batchWorker;

    public KafkaBotClient(OutboxEventSender<T> sender, BatchWorker<OutboxEventUpdateDto> batchWorker) {

        this.sender = sender;
        this.batchWorker = batchWorker;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void sendOutboxEventTypeLinkUpdate(OutboxEvent outboxEvent) {

        try {

            T event = sender.deserialize(outboxEvent.getEventBody());

            sender.send(outboxEvent.getKey(), event).whenComplete((sendResult, t) -> {
                if (t != null) {

                    log.error(
                            "Error while sending linkUpdate",
                            kv("link_update_id", sender.extractEventId(event)),
                            kv("link_update_url", sender.extractUrl(event)),
                            kv("exception_message", t.getMessage()),
                            t);

                    batchWorker.submit(new OutboxEventUpdateDto(outboxEvent.getId(), MessageStatus.FAILED));

                    return;
                }

                batchWorker.submit(new OutboxEventUpdateDto(outboxEvent.getId(), MessageStatus.SENT));
            });

        } catch (Exception e) {

            log.error("Failed to send LinkUpdateEvent", kv("outbox_event_id", outboxEvent.getId()), e);

            batchWorker.submit(new OutboxEventUpdateDto(outboxEvent.getId(), MessageStatus.FAILED));
        }
    }
}
