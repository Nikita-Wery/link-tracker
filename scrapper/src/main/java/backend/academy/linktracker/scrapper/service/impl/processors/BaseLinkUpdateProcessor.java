package backend.academy.linktracker.scrapper.service.impl;

import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.service.LinkService;
import backend.academy.linktracker.scrapper.service.LinkUpdateProcessor;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
@ConditionalOnProperty(
    name = "app.client.bot.api.kafka.enabled",
    havingValue = "false",
    matchIfMissing = true
)
public class BaseLinkUpdateProcessor implements LinkUpdateProcessor {

    private final LinkService linkService;

    @Override
    public void processBatch(List<UpdateLinkDto> batch) {
        linkService.updateLastUpdateBatch(batch, batch.size());
    }

}
