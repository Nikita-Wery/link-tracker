package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.proto.ListLinksResponse;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import java.util.List;
import java.util.Optional;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import backend.academy.linktracker.scrapper.utils.GrpcMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@AllArgsConstructor
//TODO: builder fix needed
public class SubscriptionService {

    private final ChatLinkRepository chatLinkRepository;
//    private final ChatRepository chatRepository;
//    private final LinkRepository linkRepository;
    private final ChatService chatService;
    private final LinkService linkService;
    private final GrpcMapper grpcMapper;
    private final DtoEntityMapper dtoEntityMapper;

    // TODO: добавить @Transactional
    @Transactional
    @SuppressFBWarnings(
        value = "SLF4J_PLACE_HOLDER_MISMATCH",
        justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public ChatLink trackLink(ChatLink chatLink) {

        // TODO:
        log.warn("USER ADDED IN SUBSCRIPTION SERVICE ID: {}", chatLink.getChat().getChatId());

        try {
            chatService.addChat(chatLink.getChat());
        } catch (ChatAlreadyExistsException e) {
            log.info("When adding a chatlink, either the chat already existed");
        }

        try {
            linkService.addLink(chatLink.getLink());
        } catch (LinkAlreadyTrackedException e) {
            log.info("When adding a chatlink, either the link already existed");
        }

        try {
            chatLink = chatLinkRepository.save(chatLink);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Link already tracked",
                kv("chat_id", chatLink.getChat().getChatId()),
                kv("link_url", chatLink.getLink().getUrl()),
                ex
            );
            throw new LinkAlreadyTrackedException("The link is already being tracked by the chat");
        }

        return chatLink;
    }

    @Transactional
    public ChatLink untrackLink(ChatLink chatLink) {

        Optional<ChatLink> deletedChatLinkId = chatLinkRepository.deleteChatLinkReturningChatLink(chatLink);

        if (deletedChatLinkId.isEmpty()) {
            throw new LinkNotTrackedException("The link was not tracked from the chat side");
        }

        return deletedChatLinkId.get();
    }

    // TODO: @Transactional
    @Transactional
    public List<ChatLink> getTrackedLinksByChatId(Long chatId) {

        return chatLinkRepository.findChatLinksByChatId(chatId);
    }

    // TODO: @Transactional
    @Transactional
    public List<LinkResponse> getLinkResponsesByChatId(Long chatId) {
        return getTrackedLinksByChatId(chatId).stream()
                .map(dtoEntityMapper::linkToLinkResponse)
                .toList();
    }

    // TODO: @Transactional
    @Transactional
    public ListLinksResponse getProtoListLinksResponseByChatId(long chatId) {
        return grpcMapper.listOfLinksToLinksResponse(getTrackedLinksByChatId(chatId));
    }

}
