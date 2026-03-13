package backend.academy.linktracker.bot.client;

import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface ScrapperClient {

    @PostExchange("/tg-chat/{id}")
    void registerChat(
            @PathVariable("id") Long id
    );

    @DeleteExchange("/tg-chat/{id}")
    void deleteChat(
            @PathVariable("id") Long id
    );

    @GetExchange("/links")
    ListLinksResponse getLinks(
            @RequestHeader("Tg-Chat-Id") Long chatId
    );

    @PostExchange("/links")
    LinkResponse addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestBody AddLinkRequest request
    );

    @DeleteExchange("/links")
    LinkResponse untrackLink(
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestBody RemoveLinkRequest request
    );
}
