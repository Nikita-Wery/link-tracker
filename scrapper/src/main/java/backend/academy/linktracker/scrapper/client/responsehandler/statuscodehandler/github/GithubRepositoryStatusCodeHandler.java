package backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.github;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import java.util.Set;

public class GithubRepositoryStatusCodeHandler extends GithubStatusCodeDefaultHandler {

    public GithubRepositoryStatusCodeHandler() {
        // TODO: изменить на нужный код
        super(Set.of(400));
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) {
        // TODO: добавить реализацию
    }
}
