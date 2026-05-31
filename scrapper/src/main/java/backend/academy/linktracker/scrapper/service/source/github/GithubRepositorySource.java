package backend.academy.linktracker.scrapper.service.source.github;

import backend.academy.linktracker.scrapper.client.external.GitHubClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import backend.academy.linktracker.scrapper.dto.github.RepoInfo;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
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
public class GithubRepositorySource implements UpdateSource<LinkUpdate> {

    private final GitHubClient gitHubClient;
    private final ScrapperMetricsService metricsService;

    public GithubRepositorySource(GitHubClient gitHubClient, ScrapperMetricsService metricsService) {
        this.gitHubClient = gitHubClient;
        this.metricsService = metricsService;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.GITHUB_REPOSITORY;
    }

    @Override
    @Retry(name = "githubRepositoryRetry", fallbackMethod = "fallback")
    @RateLimiter(name = "githubRepositoryLimiter")
    public List<LinkUpdate> getUpdates(Link link) {

        List<LinkUpdate> updates = new ArrayList<>();

        String[] ownerAndRepo = extractOwnerAndRepo(link);

        GithubRepositoryUpdateTime update = metricsService.timeExternalCall(
                "external_source",
                "github",
                ResourceType.GITHUB_REPOSITORY.name(),
                () -> gitHubClient.getRepositoryUpdateTime(ownerAndRepo[0], ownerAndRepo[1]));

        metricsService.incrementApiRequest("github");

        if (update.updateAt().isAfter(link.getLatestUpdateTime())) {
            updates.add(toLinkUpdate(update, link));
        }

        return updates;
    }

    private String[] extractOwnerAndRepo(Link link) {

        if (!link.getResourceType().equals(ResourceType.GITHUB_REPOSITORY)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.GITHUB_REPOSITORY);
            throw new IllegalArgumentException("Invalid GitHub URL: " + link.getUrl());
        }

        RepoInfo info = (RepoInfo) link.getResourceType().parser().parse(URI.create(link.getUrl()));
        return new String[] {info.owner(), info.repository()};
    }

    private String buildDescription(GithubRepositoryUpdateTime updateTime, Link link) {
        return "REPOSITORY=%s%nLINK=%s%nUPDATED_AT=%s%nLAST_PUSH=%s"
                .formatted(updateTime.repositoryName(), link.getUrl(), updateTime.updateAt(), updateTime.pushedAt());
    }

    private LinkUpdate toLinkUpdate(GithubRepositoryUpdateTime update, Link link) {
        return new LinkUpdate(
                link.getLinkId(),
                URI.create(link.getUrl()),
                buildDescription(update, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                update.updateAt());
    }

    public List<LinkUpdate> fallback(Link link, Throwable exception) {
        log.warn("Fallback for link url: {}", link.getUrl(), exception);
        log.warn("Github unavailable, source type {}", ResourceType.GITHUB_REPOSITORY);

        return Collections.emptyList();
    }
}
