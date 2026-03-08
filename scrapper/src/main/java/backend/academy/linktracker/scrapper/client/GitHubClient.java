package backend.academy.linktracker.scrapper.client;

import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

// TODO убрать url отсюда и перенести в config
@HttpExchange(accept = "application/vnd.github.v3+json")
public interface GitHubClient {

    //TODO: добавить токен и подумать как это лучше сделать
    @GetExchange("/repos/{owner}/{repo}")
    GithubRepositoryUpdateTime getRepositoryUpdateTime(@PathVariable String owner, @PathVariable String repo);
}
