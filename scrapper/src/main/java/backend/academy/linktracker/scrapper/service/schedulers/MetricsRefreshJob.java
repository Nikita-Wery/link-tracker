package backend.academy.linktracker.scrapper.service.schedulers;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.metrics.enabled", havingValue = "true", matchIfMissing = true)
public class MetricsRefreshJob {

    private final LinkRepository linkRepository;
    private final ScrapperMetricsService scrapperMetricsService;

    @Scheduled(fixedDelayString = "${app.scheduler.metrics.interval-ms}")
    public void refresh() {

        for (ResourceType resourceType : ResourceType.values()) {
            scrapperMetricsService.setTrackedLinksCount(
                    resourceType.name(), linkRepository.countByResourceType(resourceType));
        }
    }
}
