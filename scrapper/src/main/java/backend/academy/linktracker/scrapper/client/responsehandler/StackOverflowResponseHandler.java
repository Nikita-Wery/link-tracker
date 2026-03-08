package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.stackoverflow.StackOverflowStatusCodeDefaultHandler;
import lombok.SneakyThrows;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import java.util.List;

public class StackOverflowResponseHandler implements DefaultResponseHandler {

    private final List<StackOverflowStatusCodeDefaultHandler> codeHandlers;

    public StackOverflowResponseHandler(List<StackOverflowStatusCodeDefaultHandler> codeHandlers) {
        this.codeHandlers = codeHandlers;
    }

    @SneakyThrows
    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) {

        for (StackOverflowStatusCodeDefaultHandler codeHandler : codeHandlers) {
            if (codeHandler.supportsCode(response.getStatusCode().value())) {
                codeHandler.handle(request, response);
            }
        }
    }

}
