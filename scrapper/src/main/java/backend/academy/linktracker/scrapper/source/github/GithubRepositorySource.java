package backend.academy.linktracker.scrapper.source.github;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import backend.academy.linktracker.scrapper.source.UpdateSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Component
public class GithubRepositorySource implements UpdateSource {

    private final GitHubClient gitHubClient;

    public GithubRepositorySource(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.GITHUB_REPOSITORY;
    }

    @Override
    public UpdateEvent getUpdates(Link link) {
        String[] dataForRequest = extractOwnerAndRepo(link.getUrl());

        GithubRepositoryUpdateTime update =
                gitHubClient.getRepositoryUpdateTime(dataForRequest[0], dataForRequest[1]);

        return new LinkUpdate(
            link.getUrl(),
            buildDescription(update),
            link.getTgChatId(),
            link.getResourceType(),
            update.updateAt()
        );
    }

    protected String[] extractOwnerAndRepo(URI url) {

        Matcher matcher = ResourceType.GITHUB_REPOSITORY
                .pattern()
                .matcher(url.toString());

        if (!matcher.matches()) {
            log.error("The URL format: {}, does not match with {}", url, ResourceType.GITHUB_REPOSITORY);
            throw new IllegalArgumentException("Invalid GitHub URL: " + url);
        }

        return new String[]{
                matcher.group(1),
                matcher.group(2)
        };
    }

    protected String buildDescription(GithubRepositoryUpdateTime updateTime) {
        return "Repository %s updated at %s, last push %s"
                .formatted(updateTime.repositoryName(), updateTime.updateAt(), updateTime.pushedAt());
    }

}
