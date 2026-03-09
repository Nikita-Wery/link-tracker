package backend.academy.linktracker.scrapper.source.stackoverflow;

import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import backend.academy.linktracker.scrapper.dto.stackoverflow.StackOverflowQuestionUpdateTime;
import backend.academy.linktracker.scrapper.source.UpdateSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Component
public class StackOverflowQuestionSource implements UpdateSource {

    private final StackOverflowClient stackOverflowClient;

    public StackOverflowQuestionSource(StackOverflowClient stackOverflowClient) {
        this.stackOverflowClient = stackOverflowClient;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.STACKOVERFLOW_QUESTION;
    }

    @Override
    public UpdateEvent getUpdates(Link link) {

        String dataForRequest = extractQuestionId(link.getUrl());

        StackOverflowQuestionUpdateTime update =
                stackOverflowClient.getQuestionUpdateTime(Long.getLong(dataForRequest));

        return new LinkUpdate(
            link.getId(),
            link.getUrl(),
            buildDescription(update),
            link.getTgChatIds(),
            link.getResourceType(),
            update.lastUpdate()
        );
    }

    // TODO: возможно тут стоит возвращать long
    protected String extractQuestionId(URI url) {

        Matcher matcher = ResourceType.STACKOVERFLOW_QUESTION
            .pattern()
            .matcher(url.toString());

        if (!matcher.matches()) {
            log.error("The URL format: {}, does not match with {}", url, ResourceType.STACKOVERFLOW_QUESTION);
            throw new IllegalArgumentException("Invalid StackOverflow URL: " + url);
        }

        return matcher.group(1);
    }

    protected String buildDescription(StackOverflowQuestionUpdateTime updateTime) {
        return "QuestionId %s updated at %s, last push %s"
            .formatted(updateTime.questionId(), updateTime.lastUpdate(), updateTime.lastEdit());
    }

}
