package backend.academy.linktracker.scrapper.service.source.stackoverflow;

import backend.academy.linktracker.scrapper.client.external.StackOverflowClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionUpdateTime;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowQuestionSource implements UpdateSource<LinkUpdate> {

    private final StackOverflowClient stackOverflowClient;
    private final StackoverflowProperties stackoverflowProperties;

    public StackOverflowQuestionSource(
            StackOverflowClient stackOverflowClient, StackoverflowProperties stackoverflowProperties) {

        this.stackoverflowProperties = stackoverflowProperties;
        this.stackOverflowClient = stackOverflowClient;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.STACKOVERFLOW_QUESTION;
    }

    @Override
    @Retry(name = "stackQuestionRetry", fallbackMethod = "fallback")
    @RateLimiter(name = "stackQuestionLimiter")
    public List<LinkUpdate> getUpdates(Link link) {

        List<LinkUpdate> updates = new ArrayList<>();

        Long questionId = extractQuestionId(link);

        StackOverflowQuestionUpdateTime update = stackOverflowClient.getQuestionUpdateTime(
                questionId,
                "stackoverflow",
                stackoverflowProperties.getKey(),
                stackoverflowProperties.getAccessToken());

        if (update.lastUpdate().isAfter(link.getLatestUpdateTime())) {
            updates.add(toLinkUpdate(update, link));
        }

        return updates;
    }

    private Long extractQuestionId(Link link) {

        if (!link.getResourceType().equals(ResourceType.STACKOVERFLOW_QUESTION)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.STACKOVERFLOW_QUESTION);
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + link.getUrl());
        }

        return (Long) link.getResourceType().parser().parse(URI.create(link.getUrl()));
    }

    private String buildDescription(StackOverflowQuestionUpdateTime updateTime, Link link) {
        return "Вопрос с id %s, по ссылке %s, обновлён в %s, последний push %s"
                .formatted(updateTime.questionId(), link.getUrl(), updateTime.lastUpdate(), updateTime.lastEdit());
    }

    private LinkUpdate toLinkUpdate(StackOverflowQuestionUpdateTime update, Link link) {
        return new LinkUpdate(
                link.getLinkId(),
                URI.create(link.getUrl()),
                buildDescription(update, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                update.lastUpdate());
    }

    public List<LinkUpdate> fallback(Link link, Throwable exception) {
        log.warn("Fallback link url: {}", link.getUrl(), exception);
        log.warn("StackOverflow unavailable, source type {}", ResourceType.STACKOVERFLOW_QUESTION);

        return Collections.emptyList();
    }
}
