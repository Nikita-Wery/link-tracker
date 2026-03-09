package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.dto.bot.ListLinkResponse;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.exception.ChatNotExistException;
import backend.academy.linktracker.scrapper.exception.LinkNotSupportedException;
import backend.academy.linktracker.scrapper.mapper.LinkMapper;
import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.service.LinkService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkService linksService;
    private final ChatService chatService;
    private final LinkMapper linkMapper;

    public LinkController(
            LinkService linksService,
            ChatService chatService,
            LinkMapper linkMapper) {

        this.linksService = linksService;
        this.chatService = chatService;
        this.linkMapper = linkMapper;
    }

    @GetMapping
    public ListLinkResponse getLinks(
            @RequestHeader("Tg-Chat-Id") Long chatId
    ) {
        List<LinkResponse> linkResponses =
            chatService.getChatById(chatId).map(chat
                    -> chat.getTrackedLinks().stream().map(linkMapper::linkToLinkResponse).toList())
                .orElseThrow(() -> new ChatNotExistException("Chat not found"));

        return new ListLinkResponse(linkResponses, linkResponses.size());
    }

    /*
        В будущем с добавлением Postgres, возможно достаточно будет chatService
        в котором можно будет отловить PK/FK exception
     */
    // TODO: добавить логгер
    @PostMapping
    public LinkResponse addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestBody AddLinkRequest request
    ) {
        Optional<Link> link = linksService.getLinkByURI(request.link());

        if (link.isPresent()) {
            return linkMapper.linkToLinkResponse(chatService.addLinkToChat(link.get(), chatId));
        } else {
            throw new LinkNotSupportedException("The added link is not supported");
        }

    }

    /*
        В будущем подумать на PK у Link и не ходить за
        Link в репо
     */
    // TODO: добавить логгер
    @DeleteMapping
    public LinkResponse removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId,
            @RequestBody RemoveLinkRequest request
    ) {
        Optional<Link> link = linksService.getLinkByURI(request.link());

        if (link.isPresent()) {
            chatService.untrackLink(link.get(), chatId);

            return linkMapper.linkToLinkResponse(link.get());
        } else {
            throw new LinkNotSupportedException("The link being deleted is not supported");
        }

    }
}
