package backend.academy.linktracker.scrapper.service.impl.processors;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateProcessor;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "false")
public class BaseLinkUpdateProcessor implements LinkUpdateProcessor {

    private final LinkService linkService;

    @Override
    public void processBatch(List<LinkUpdate> batch) {
        linkService.updateLastUpdateBatch(batch, batch.size());
        log.info("Links in batch have been successfully updated");
    }
}
