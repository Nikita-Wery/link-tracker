package backend.academy.linktracker.scrapper.client.inner.impl;

import backend.academy.linktracker.scrapper.client.inner.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
@CircuitBreaker(name = "botApi")
public interface RestBotClient extends BotClient<LinkUpdate> {

    @RateLimiter(name = "sendUpdateLimiter")
    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdate update);
}
