package backend.academy.linktracker.scrapper.controller.rest;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.dto.bot.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
public class LinkController {

    private final ChatService chatService;
    private final SubscriptionService subscriptionService;
    private final DtoEntityMapper dtoEntityMapper;

    public LinkController(
            ChatService chatService, SubscriptionService subscriptionService, DtoEntityMapper dtoEntityMapper) {

        this.chatService = chatService;
        this.subscriptionService = subscriptionService;
        this.dtoEntityMapper = dtoEntityMapper;
    }

    @GetMapping
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<LinkResponse> linkResponses = subscriptionService.getLinkResponsesByChatId(chatId);

        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    @PostMapping
    public LinkResponse trackLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody AddLinkRequest request) {

        Link link = dtoEntityMapper.linkFromAddLinkRequest(request);
        Chat chat = dtoEntityMapper.getChatFromChatId(chatId);
        ChatLink chatLink = new ChatLink(link, chat, new HashSet<>(request.tags()));

        return dtoEntityMapper.linkToLinkResponse(subscriptionService.trackLink(chatLink));
    }

    @DeleteMapping
    public LinkResponse untrackLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        Link link = dtoEntityMapper.linkFromRemoveLinkRequest(request);
        Chat chat = dtoEntityMapper.getChatFromChatId(chatId);
        ChatLink chatLink = new ChatLink(link, chat);

        return dtoEntityMapper.linkToLinkResponse(subscriptionService.untrackLink(chatLink));
    }
}
