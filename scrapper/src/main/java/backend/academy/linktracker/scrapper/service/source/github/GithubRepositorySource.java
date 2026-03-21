package backend.academy.linktracker.scrapper.service.source.github;

import backend.academy.linktracker.scrapper.client.external.GitHubClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        String[] ownerAndRepo = extractOwnerAndRepo(link);

        GithubRepositoryUpdateTime update = gitHubClient.getRepositoryUpdateTime(ownerAndRepo[0], ownerAndRepo[1]);

        return new LinkUpdate(
                link.getId(),
                link.getUrl(),
                buildDescription(update, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                update.updateAt());
    }

    protected String[] extractOwnerAndRepo(Link link) {

        if (!link.getResourceType().equals(ResourceType.GITHUB_REPOSITORY)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.GITHUB_REPOSITORY);
            throw new IllegalArgumentException("Invalid GitHub URL: " + link.getUrl());
        }

        return (String[]) link.getResourceType().parser().parse(link.getUrl());
    }

    protected String buildDescription(GithubRepositoryUpdateTime updateTime, Link link) {
        return "Репозиторий %s, по ссылке %s, обновлён в %s, последний push %s"
                .formatted(updateTime.repositoryName(), link.getUrl(), updateTime.updateAt(), updateTime.pushedAt());
    }
}
