package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
@CircuitBreaker(name = "scrapperApi")
public interface ScrapperClient {

    @Retry(name = "registerChatRetry")
    @RateLimiter(name = "registerChatLimiter")
    @PostExchange("/tg-chat/{id}")
    void registerChat(@PathVariable("id") Long id);

    @Retry(name = "deleteChatRetry")
    @RateLimiter(name = "deleteChatLimiter")
    @DeleteExchange("/tg-chat/{id}")
    void deleteChat(@PathVariable("id") Long id);

    @Retry(name = "getLinksRetry")
    @RateLimiter(name = "getLinksLimiter")
    @GetExchange("/links")
    ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long chatId);

    @Retry(name = "addLinkRetry")
    @RateLimiter(name = "addLinkLimiter")
    @PostExchange("/links")
    LinkResponse addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request);

    @Retry(name = "untrackLinkRetry")
    @RateLimiter(name = "untrackLinkLimiter")
    @DeleteExchange("/links")
    LinkResponse untrackLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request);
}
