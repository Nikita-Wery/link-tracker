package backend.academy.linktracker.scrapper.service.source.github;

import backend.academy.linktracker.scrapper.client.external.GitHubClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.github.GithubIssueResponse;
import backend.academy.linktracker.scrapper.dto.github.RepoInfo;
import backend.academy.linktracker.scrapper.service.source.UpdateSource;
import backend.academy.linktracker.scrapper.utils.TextMessageHandler;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubRepositoryIssueSource implements UpdateSource<LinkUpdate> {

    private final GitHubClient gitHubClient;

    public GithubRepositoryIssueSource(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.GITHUB_REPOSITORY_ISSUE;
    }

    @Override
    @Retry(name = "githubIssuesRetry", fallbackMethod = "fallback")
    @RateLimiter(name = "githubIssuesLimiter")
    public List<LinkUpdate> getUpdates(Link link) {

        String[] linkData = extractLinkData(link);

        List<GithubIssueResponse> issuesResponse = gitHubClient.getRepositoryIssueUpdate(linkData[0], linkData[1]);

        return issuesResponse.stream()
                .filter(issue -> issue.createdAt().isAfter(link.getLatestUpdateTime()))
                .map(issue -> toLinkUpdate(issue, link))
                .toList();
    }

    private String[] extractLinkData(Link link) {

        if (!link.getResourceType().equals(ResourceType.GITHUB_REPOSITORY_ISSUE)) {
            log.error("The URL format: {}, does not match with {}", link.getUrl(), ResourceType.GITHUB_REPOSITORY);
            throw new IllegalArgumentException("Invalid GitHub URL: " + link.getUrl());
        }

        RepoInfo info = (RepoInfo) link.getResourceType().parser().parse(URI.create(link.getUrl()));
        return new String[] {info.owner(), info.repository()};
    }

    private String buildDescription(GithubIssueResponse response, Link link) {
        return "Github issue: %s, с автором %s, по ссылке %s%nТекст issue %s%nБыл обновлён %s"
                .formatted(
                        response.title(),
                        response.user().login(),
                        link.getUrl(),
                        TextMessageHandler.shortenMessage(response.body()),
                        response.updatedAt());
    }

    private LinkUpdate toLinkUpdate(GithubIssueResponse response, Link link) {
        return new LinkUpdate(
                link.getLinkId(),
                URI.create(link.getUrl()),
                buildDescription(response, link),
                link.getTrackingChats().stream()
                        .map(chatLink -> chatLink.getChat().getChatId())
                        .collect(Collectors.toSet()),
                link.getResourceType(),
                response.updatedAt());
    }

    public List<LinkUpdate> fallback(Link link, Throwable exception) {
        log.warn("Fallback link url: {}", link.getUrl(), exception);
        log.warn("Github unavailable, source type {}", ResourceType.GITHUB_REPOSITORY_ISSUE);

        return Collections.emptyList();
    }
}
