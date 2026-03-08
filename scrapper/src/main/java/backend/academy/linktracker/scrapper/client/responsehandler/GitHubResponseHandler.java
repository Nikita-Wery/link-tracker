package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.bot.BotStatusCodeDefaultHandler;
import backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.github.GithubStatusCodeDefaultHandler;
import lombok.SneakyThrows;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import java.util.List;

public class GitHubResponseHandler implements DefaultResponseHandler {

    private final List<GithubStatusCodeDefaultHandler> codeHandlers;

    public GitHubResponseHandler(List<GithubStatusCodeDefaultHandler> codeHandlers) {
        this.codeHandlers = codeHandlers;
    }

    @SneakyThrows
    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) {

        for (GithubStatusCodeDefaultHandler codeHandler : codeHandlers) {
            if (codeHandler.supportsCode(response.getStatusCode().value())) {
                codeHandler.handle(request, response);
            }
        }
    }

}
