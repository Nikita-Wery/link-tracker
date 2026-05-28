package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.client.KafkaBotClient;
import backend.academy.linktracker.ai.model.Priority;
import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RawLinkUpdatesProcessor {

    private final List<RawLinkUpdateFilter> filters;
    private final SummarizationService summarizationService;
    private final KafkaBotClient botClient;

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

        String resultDescription = summarizationService.summarizeDescription(rawLinkUpdate.description());

        botClient
                .send(messageKey, rawLinkUpdateToProcessed(rawLinkUpdate, resultDescription))
                .join();

        ack.acknowledge();
    }

    private ProcessedLinkUpdate rawLinkUpdateToProcessed(RawLinkUpdate rawLinkUpdate, String resultDescription) {
        return new ProcessedLinkUpdate(
                rawLinkUpdate.id(),
                rawLinkUpdate.url(),
                resultDescription,
                rawLinkUpdate.tgChatIds(),
                Priority.HIGH.name());
    }
}
