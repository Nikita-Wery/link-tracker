package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.model.LinkUpdateContext;
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
    private final List<SubProcessor<LinkUpdateContext>> processors;
    private final GroupingService groupingService;

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

        LinkUpdateContext context =
                LinkUpdateContext.builder().rawLinkUpdate(rawLinkUpdate).build();

        processors.forEach(processor -> processor.process(context));

        groupingService.addProcessedLinkUpdate(messageKey, rawLinkUpdateToProcessed(context));

        ack.acknowledge();
    }

    private ProcessedLinkUpdate rawLinkUpdateToProcessed(LinkUpdateContext context) {
        return new ProcessedLinkUpdate(
                context.getRawLinkUpdate().id(),
                context.getRawLinkUpdate().url(),
                context.getSummarizedDescription(),
                context.getRawLinkUpdate().tgChatIds(),
                context.getPriority());
    }
}
