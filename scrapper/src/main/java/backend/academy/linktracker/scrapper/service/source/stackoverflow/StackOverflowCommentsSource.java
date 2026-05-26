package backend.academy.linktracker.scrapper.service.source.stackoverflow;

import backend.academy.linktracker.scrapper.client.external.StackOverflowClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowWrapper;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import backend.academy.linktracker.scrapper.utils.TextMessageHandler;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowCommentsSource implements UpdateSource<LinkUpdate> {

    private final StackOverflowClient stackOverflowClient;

    public StackOverflowCommentsSource(StackOverflowClient stackOverflowClient) {

        this.stackOverflowClient = stackOverflowClient;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.STACKOVERFLOW_COMMENTS;
    }

    @Override
    @Retry(name = "stackCommentRetry", fallbackMethod = "fallback")
    @RateLimiter(name = "stackCommentLimiter")
    public List<LinkUpdate> getUpdates(Link link) {

        Long questionId = extractQuestionId(link);

        StackOverflowWrapper<StackOverflowCommentResponse> response =
                stackOverflowClient.getCommentUpdates(questionId, "stackoverflow");

        return response.items().stream()
                .filter(comment -> Instant.ofEpochSecond(comment.createdAt())
                        .isAfter(link.getLatestUpdateTime().toInstant()))
                .map(comment -> toLinkUpdate(comment, link))
                .toList();
    }

    private Long extractQuestionId(Link link) {

        if (!link.getResourceType().equals(ResourceType.STACKOVERFLOW_COMMENTS)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.STACKOVERFLOW_COMMENTS);
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + link.getUrl());
        }

        return (Long) link.getResourceType().parser().parse(URI.create(link.getUrl()));
    }

    private String buildDescription(StackOverflowCommentResponse update, Link link) {
        return "Добавлен комментарий с id: %s, по ссылке %s, в %s%nПользователь добавивший комментарий: %s%nТекст комментария: %s"
                .formatted(
                        update.id(),
                        link.getUrl(),
                        update.user().name(),
                        Instant.ofEpochSecond(update.createdAt()),
                        TextMessageHandler.shortenMessage(update.body()));
    }

    private LinkUpdate toLinkUpdate(StackOverflowCommentResponse update, Link link) {
        return new LinkUpdate(
                link.getLinkId(),
                URI.create(link.getUrl()),
                buildDescription(update, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                Instant.ofEpochSecond(update.createdAt())
                        .atOffset(link.getLatestUpdateTime().getOffset()));
    }

    public List<LinkUpdate> fallback(Link link, Throwable exception) {
        log.warn("Fallback link url: {}", link.getUrl(), exception);
        log.warn("StackOverflow unavailable, source type {}", ResourceType.STACKOVERFLOW_COMMENTS);

        return Collections.emptyList();
    }
}
