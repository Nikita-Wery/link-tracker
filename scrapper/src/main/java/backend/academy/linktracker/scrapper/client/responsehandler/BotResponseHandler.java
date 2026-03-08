package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.bot.BotStatusCodeDefaultHandler;
import lombok.SneakyThrows;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BotResponseHandler implements DefaultResponseHandler {

    private final List<BotStatusCodeDefaultHandler> codeHandlers;

    public BotResponseHandler(List<BotStatusCodeDefaultHandler> codeHandlers) {
        this.codeHandlers = codeHandlers;
    }

    @SneakyThrows
    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) {

        for (BotStatusCodeDefaultHandler codeHandler : codeHandlers) {
            if (codeHandler.supportsCode(response.getStatusCode().value())) {
                codeHandler.handle(request, response);
            }
        }

    }

}
