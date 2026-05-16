package backend.academy.linktracker.scrapper.service.impl.processors;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateProcessor;
import backend.academy.linktracker.scrapper.service.OutboxEventService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxLinkUpdateProcessor implements LinkUpdateProcessor {

    private final LinkService linkService;
    private final OutboxEventService outboxService;
    private final ObjectMapper objectMapper;
    private final LinkUpdateTopicProperties linkUpdateTopicProperties;

    @Override
    @Transactional
    public void processBatch(List<LinkUpdate> batch) {

        linkService.updateLastUpdateBatch(batch, batch.size());

        log.info("Links in batch have been successfully updated, addition to the outbox is expected.");

        List<OutboxEvent> outboxEvents = new ArrayList<>();

        for (LinkUpdate linkUpdate : batch) {

            try {

                String json = objectMapper.writeValueAsString(linkUpdate);

                outboxEvents.add(new OutboxEvent(linkUpdate.id(), linkUpdateTopicProperties.getName(), json));
            } catch (Exception e) {
                log.error("Error generating JSON from linkUpdate");
            }
        }

        outboxService.saveOutboxEvents(outboxEvents);

        log.info(
                "The LinkUpdate batch has been successfully converted into an event and added to the outbox repository.");
    }
}
