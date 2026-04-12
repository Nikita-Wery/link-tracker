package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.proto.ListLinksResponse;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import backend.academy.linktracker.scrapper.utils.GrpcMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class SubscriptionService {

    private final ChatLinkRepository chatLinkRepository;
    private final ChatService chatService;
    private final LinkService linkService;
    private final GrpcMapper grpcMapper;
    private final DtoEntityMapper dtoEntityMapper;

    @Transactional
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public ChatLink trackLink(ChatLink chatLink) {

        try {
            chatService.addChat(chatLink.getChat());
        } catch (ChatAlreadyExistsException e) {
            log.info("When adding a chatlink, either the chat already existed");
        }

        try {
            linkService.addLink(chatLink.getLink());
        } catch (LinkAlreadyExistsException e) {
            log.info("When adding a chatlink, either the link already existed");
        }

        try {

            return chatLinkRepository.saveAndFlush(chatLink);
        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "Link already tracked",
                    kv("chat_id", chatLink.getChat().getChatId()),
                    kv("chat_link_id", chatLink.getChatLinkId()),
                    ex);
            throw new LinkAlreadyTrackedException("The link is already being tracked by the chat");
        }
    }

    @Transactional
    public ChatLink untrackLink(ChatLink chatLink) {

        return chatLinkRepository
                .deleteChatLinkReturningChatLink(chatLink)
                .orElseThrow(() -> new LinkNotTrackedException("The link was not tracked from the chat side"));
    }

    @Transactional(readOnly = true)
    public List<ChatLink> getTrackedLinksByChatId(Long chatId) {

        return chatLinkRepository.findChatLinksByChatId(chatId);
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> getLinkResponsesByChatId(Long chatId) {

        return getTrackedLinksByChatId(chatId).stream()
                .map(dtoEntityMapper::linkToLinkResponse)
                .toList();
    }

    @Transactional
    public ListLinksResponse getProtoListLinksResponseByChatId(long chatId) {
        return grpcMapper.listOfLinksToLinksResponse(getTrackedLinksByChatId(chatId));
    }
}
