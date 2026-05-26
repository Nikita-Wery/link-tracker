package backend.academy.linktracker.scrapper.client.external;

import backend.academy.linktracker.scrapper.dto.github.GithubIssueResponse;
import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/vnd.github.v3+json")
@CircuitBreaker(name = "githubApi")
public interface GitHubClient {

    @GetExchange("/repos/{owner}/{repo}")
    GithubRepositoryUpdateTime getRepositoryUpdateTime(@PathVariable String owner, @PathVariable String repo);

    @GetExchange("/repos/{owner}/{repo}/issues")
    List<GithubIssueResponse> getRepositoryIssueUpdate(@PathVariable String owner, @PathVariable String repo);
}
