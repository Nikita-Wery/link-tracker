package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import backend.academy.linktracker.scrapper.source.UpdateSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LinkUpdateService {

    private final Map<ResourceType, UpdateSource> sourceMap;
    private final ApplicationEventPublisher publisher;
    private final LinkService linkService;

    public LinkUpdateService(List<UpdateSource> sources,
                             ApplicationEventPublisher publisher,
                             LinkService linkService) {

            this.linkService = linkService;
            this.publisher = publisher;
            this.sourceMap = sources.stream()
                .collect(Collectors.toMap(UpdateSource::getResourceType,
                                          Function.identity()));
    }

    public void process(Link link) {

        UpdateSource source = sourceMap.get(link.getResourceType());

        if (source == null) {
            log.error("No handler found for the link: {}", link.getUrl());
            throw new IllegalStateException("No source for " + link.getResourceType());
        }

        UpdateEvent updateEvent = source.getUpdates(link);

        if (updateEvent.getLastUpdate().isAfter(link.getLatestUpdateTime())) {
            linkService.changeLastUpdate(link, updateEvent.getLastUpdate());

            publisher.publishEvent(updateEvent);
        } else {
            log.info("Link {} has not been updated.", link.getUrl());
        }

    }

}
