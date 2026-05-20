package backend.academy.linktracker.scrapper.controller.rest;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.dto.bot.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.subscriptionimpl.SubscriptionServiceBase;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/links")
public class LinkController {

    private final SubscriptionServiceBase subscriptionServiceBase;
    private final DtoEntityMapper dtoEntityMapper;

    public LinkController(SubscriptionServiceBase subscriptionServiceBase, DtoEntityMapper dtoEntityMapper) {

        this.subscriptionServiceBase = subscriptionServiceBase;
        this.dtoEntityMapper = dtoEntityMapper;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @GetMapping
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.info("get_links", kv("chat_id", chatId));

        List<LinkResponse> linkResponses = subscriptionServiceBase.getLinkResponsesByChatId(chatId);

        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @PostMapping
    public LinkResponse trackLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody AddLinkRequest request) {
        log.info("track_link", kv("chat_id", chatId), kv("link_url", request.link()));

        Link link = dtoEntityMapper.linkFromAddLinkRequest(request);
        Chat chat = dtoEntityMapper.getChatFromChatId(chatId);
        ChatLink chatLink = new ChatLink(link, chat, new HashSet<>(request.tags()));

        return subscriptionServiceBase.trackLinkReturnLinkResponse(chatLink);
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @DeleteMapping
    public LinkResponse untrackLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        log.info("untrack_link", kv("chat_id", chatId), kv("link_url", request.link()));

        Link link = dtoEntityMapper.linkFromRemoveLinkRequest(request);
        Chat chat = dtoEntityMapper.getChatFromChatId(chatId);
        ChatLink chatLink = new ChatLink(link, chat);

        return subscriptionServiceBase.untrackLinkReturnLinkResponse(chatLink);
    }
}
