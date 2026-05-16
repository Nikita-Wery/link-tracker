package backend.academy.linktracker.scrapper.service.impl;

import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateProcessor;
import backend.academy.linktracker.scrapper.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(
    name = "app.client.bot.api.kafka.enabled",
    havingValue = "true"
)
@RequiredArgsConstructor
public class OutboxLinkUpdateProcessor implements LinkUpdateProcessor {

    private final LinkService linkService;
    private final OutboxEventService outboxService;

    @Override
    @Transactional
    public void processBatch(List<UpdateLinkDto> batch) {

        linkService.updateLastUpdateBatch(batch, batch.size());

        log.info("Links was sucsessfully updated");



        outboxService.saveBatch(batch);
    }
}
