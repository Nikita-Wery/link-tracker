package backend.academy.linktracker.scrapper.service.source.stackoverflow;

import backend.academy.linktracker.scrapper.client.external.StackOverflowClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowWrapper;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import backend.academy.linktracker.scrapper.utils.TextMessageHandler;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowAnswersSource implements UpdateSource<LinkUpdate> {

    private final StackOverflowClient stackOverflowClient;
    private final ScrapperMetricsService scrapperMetricsService;

    public StackOverflowAnswersSource(
            StackOverflowClient stackOverflowClient, ScrapperMetricsService scrapperMetricsService) {
        this.stackOverflowClient = stackOverflowClient;
        this.scrapperMetricsService = scrapperMetricsService;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.STACKOVERFLOW_ANSWERS;
    }

    @Override
    @Retry(name = "stackAnswerRetry", fallbackMethod = "fallback")
    @RateLimiter(name = "stackAnswerLimiter")
    public List<LinkUpdate> getUpdates(Link link) {

        Long questionId = extractQuestionId(link);

        StackOverflowWrapper<StackOverflowAnswerResponse> response = scrapperMetricsService.timeExternalCall(
                "external_source",
                "stackoverflow",
                ResourceType.STACKOVERFLOW_ANSWERS.name(),
                () -> stackOverflowClient.getAnswerUpdates(questionId, "stackoverflow"));

        scrapperMetricsService.incrementApiRequest("stackoverflow");

        return response.items().stream()
                .filter(answer -> Instant.ofEpochSecond(answer.updatedAt())
                        .isAfter(link.getLatestUpdateTime().toInstant()))
                .map(answer -> toLinkUpdate(answer, link))
                .sorted(Comparator.comparing(LinkUpdate::getLastUpdate))
                .toList();
    }

    private Long extractQuestionId(Link link) {

        if (!link.getResourceType().equals(ResourceType.STACKOVERFLOW_ANSWERS)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.STACKOVERFLOW_ANSWERS);
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + link.getUrl());
        }

        return (Long) link.getResourceType().parser().parse(URI.create(link.getUrl()));
    }

    private String buildDescription(StackOverflowAnswerResponse update, Link link) {
        return "ID=%s%nLINK=%s%nUPDATED_AT=%s%nAUTHOR=%s%nTEXT=%s"
                .formatted(
                        update.id(),
                        link.getUrl(),
                        Instant.ofEpochSecond(update.updatedAt()),
                        update.user().name(),
                        TextMessageHandler.shortenMessage(update.body()));
    }

    private LinkUpdate toLinkUpdate(StackOverflowAnswerResponse update, Link link) {
        return new LinkUpdate(
                link.getLinkId(),
                URI.create(link.getUrl()),
                buildDescription(update, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                Instant.ofEpochSecond(update.updatedAt())
                        .atOffset(link.getLatestUpdateTime().getOffset()));
    }

    public List<LinkUpdate> fallback(Link link, Throwable exception) {
        log.warn("Fallback link url: {}", link.getUrl(), exception);
        log.warn("StackOverflow unavailable, source type {}", ResourceType.STACKOVERFLOW_ANSWERS);

        return Collections.emptyList();
    }
}
