package backend.academy.linktracker.scrapper.service.source.stackoverflow;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionUpdateTime;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StackOverflowQuestionSource implements UpdateSource {

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
    public UpdateEvent getUpdates(Link link) {

        Long questionId = extractQuestionId(link);

        StackOverflowQuestionUpdateTime update = stackOverflowClient.getQuestionUpdateTime(
                questionId,
                "stackoverflow",
                stackoverflowProperties.getKey(),
                stackoverflowProperties.getAccessToken());

        return new LinkUpdate(
                link.getId(),
                link.getUrl(),
                buildDescription(update, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                update.lastUpdate());
    }

    protected Long extractQuestionId(Link link) {

        if (!link.getResourceType().equals(ResourceType.STACKOVERFLOW_QUESTION)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.STACKOVERFLOW_QUESTION);
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + link.getUrl());
        }

        return (Long) link.getResourceType().parser().parse(link.getUrl());
    }

    protected String buildDescription(StackOverflowQuestionUpdateTime updateTime, Link link) {
        return "Вопрос с id %s, по ссылке %s, обновлён в %s, последний push %s"
                .formatted(updateTime.questionId(), link.getUrl(), updateTime.lastUpdate(), updateTime.lastEdit());
    }
}
