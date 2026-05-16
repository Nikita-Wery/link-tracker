package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkUpdateService {

    @Value("${app.client.bot.api.kafka.enabled:false}")
    private boolean kafkaIsEnabled;

    private final Map<ResourceType, UpdateSource<LinkUpdate>> sourceMap;
    private final ApplicationEventPublisher publisher;
    private final BatchWorker<LinkUpdate> linkUpdateWorker;

    public LinkUpdateService(
            List<UpdateSource<LinkUpdate>> sources,
            ApplicationEventPublisher publisher,
            BatchWorker<LinkUpdate> linkUpdateWorker) {

        this.linkUpdateWorker = linkUpdateWorker;
        this.publisher = publisher;
        this.sourceMap = sources.stream().collect(Collectors.toMap(UpdateSource::getResourceType, Function.identity()));
    }

    public void process(Link link) {

        UpdateSource<LinkUpdate> source = sourceMap.get(link.getResourceType());

        if (source == null) {
            log.error("No handler found for the link: {}", link.getUrl());
            return;
        }

        try {

            List<LinkUpdate> updateEvents = source.getUpdates(link);

            if (updateEvents.isEmpty()) {
                log.info("Link {} has not been updated", link.getUrl());
                return;
            }

            LinkUpdate latestEvent = updateEvents.stream()
                    .max(Comparator.comparing(LinkUpdate::lastUpdate))
                    .orElseThrow();

            linkUpdateWorker.submit(latestEvent);

            if (!kafkaIsEnabled) {
                publisher.publishEvent(latestEvent);
            }

        } catch (ExternalApiException e) {
            log.warn("API exception was catched in {}", Thread.currentThread().getName());
        }
    }
}
