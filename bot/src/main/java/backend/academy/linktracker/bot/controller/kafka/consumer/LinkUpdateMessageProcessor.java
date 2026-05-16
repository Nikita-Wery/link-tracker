package backend.academy.linktracker.bot.controller.kafka.consumer;

import backend.academy.linktracker.bot.domain.ProcessedMessage;
import backend.academy.linktracker.bot.service.LinkUpdateService;
import backend.academy.linktracker.bot.service.ProcessedMessagesService;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkUpdateMessageConsumer {

    private final LinkUpdateService linkUpdateService;
    private final ProcessedMessagesService processedMessagesService;

    @SuppressFBWarnings(
        value = "SLF4J_PLACE_HOLDER_MISMATCH",
        justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void process(
            Long messageKey,
            String topic,
            LinkUpdate linkUpdate,
            Acknowledgment ack) {

        log.info(
                "Received event from topic: {}, linkUpdate id {}, updated url {}",
                topic,
                linkUpdate.id(),
                linkUpdate.url()
        );

        Optional<ProcessedMessage> processed =
                processedMessagesService.findProcessedMessageById(linkUpdate.id());

        if (processed.isPresent()) {

            log.warn(
                    "Found already processed message with id {}, the message will not be sent.",
                    linkUpdate.id(),
                    kv("link_url", linkUpdate.url())
            );

            ack.acknowledge();
            return;
        }

        linkUpdateService.sendUpdateMessage(linkUpdate);

        processedMessagesService.save(
                new ProcessedMessage(messageKey)
        );

        ack.acknowledge();
    }
}
