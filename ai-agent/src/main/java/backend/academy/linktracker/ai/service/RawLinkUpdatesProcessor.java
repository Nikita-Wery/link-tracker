package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.client.KafkaBotClient;
import backend.academy.linktracker.ai.domain.ProcessedMessage;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.ai.properties.SummarizationProperties;
import backend.academy.linktracker.ai.repository.ProcessedMessagesRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class RawLinkUpdatesProcessor {

    private final List<RawLinkUpdateFilter> filters;
    private final SummarizationService summarizationService;
    private final KafkaBotClient botClient;
    private final ProcessedMessagesService processedMessagesService;

    @SuppressFBWarnings(
        value = "SLF4J_PLACE_HOLDER_MISMATCH",
        justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void processRawLinkUpdate(Long messageKey, String topic, RawLinkUpdate rawLinkUpdate, Acknowledgment ack) {

        log.info(
            "Received event from topic: {}, rawlinkUpdate id {}, updated url {}",
            topic,
            rawLinkUpdate.id(),
            rawLinkUpdate.url());

        if (!filters.stream().allMatch(filter -> filter.filter(rawLinkUpdate))) {

            ack.acknowledge();
            return;
        }

        Optional<ProcessedMessage> processed = processedMessagesService.findProcessedMessageById(messageKey);

        if (processed.isPresent()) {

            log.warn(
                "Found already processed message with id {}, the message will not be sent.",
                rawLinkUpdate.id(),
                kv("link_url", rawLinkUpdate.url()));

            ack.acknowledge();
            return;
        }

        String resultDescription = summarizationService.summarizeDescription(rawLinkUpdate.description());

        // TODO заменить
        botClient.send(rawLinkUpdateToProcessed(rawLinkUpdate, resultDescription));

        processedMessagesService.save(new ProcessedMessage(messageKey));

        ack.acknowledge();
    }

    private ProcessedLinkUpdate rawLinkUpdateToProcessed(RawLinkUpdate rawLinkUpdate, String resultDescription) {
        return new
            ProcessedLinkUpdate(
                rawLinkUpdate.id(),
                rawLinkUpdate.url(),
                resultDescription,
                rawLinkUpdate.tgChatIds(),
                Priority.HIGH.name()
            );
    }

}
