package backend.academy.linktracker.scrapper.client.inner.impl;

import backend.academy.linktracker.scrapper.client.inner.BotClient;
import backend.academy.linktracker.scrapper.domain.MessageStatus;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface RestBotClient extends BotClient<LinkUpdate> {

    @PostExchange("/updates")
    CompletableFuture<MessageStatus> sendUpdate(@RequestBody LinkUpdate update);
}
