package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class SubscriptionService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final ChatLinkRepository chatLinkRepository;

    public SubscriptionService(
            ChatRepository chatRepository,
            LinkRepository linkRepository,
            ChatLinkRepository chatLinkRepository) {

        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.chatLinkRepository = chatLinkRepository;
    }

    public ChatLink trackLink(Chat chat,
                              Link link,
                              Set<String> filters,
                              Set<String> tags) {
        ChatLink chatLink
            = ChatLink.builder()
            .chat(chat)
            .link(link)
            .filters(filters)
            .tags(tags)
            .build();

        Optional<ChatLink> chatLinkOptional = chatLinkRepository.findChatLinkById(chatLink.getId());
        Optional<Chat> chatOpt = chatRepository.findChatById(chat.getChatId());
        Optional<Link> linkOpt = linkRepository.findLinkByURI(link.getUrl());

        if (chatLinkOptional.isPresent()) {
            throw new LinkAlreadyTrackedException("The link is already being tracked by the chat");
        }

        if (chatOpt.isPresent()) {
            chatOpt.get().getTrackedLinks().add(chatLink);
        } else {
            chatRepository.save(chat);
        }

        if (linkOpt.isPresent()) {
            linkOpt.get().getTrackingChats().add(chatLink);
        } else {
            linkRepository.save(link);
        }

        return chatLinkRepository.save(chatLink);
    }

    public ChatLink untrackLink(Chat chat, Link link) {

        ChatLink.Id chatLinkId = new ChatLink.Id(link.getId(), chat.getChatId());
        Optional<ChatLink> chatLinkOpt = chatLinkRepository.findChatLinkById(chatLinkId);
        Optional<Chat> chatOptional = chatRepository.findChatById(chat.getChatId());
        Optional<Link> linkOptional = linkRepository.findLinkByURI(link.getUrl());

        if (chatOptional.isEmpty()) {
            throw new ChatNotExistsException("The chat you are trying to unpin the link from does not exist");
        }

        if (linkOptional.isEmpty()) {
            throw new LinkNotExistsException("The link you are trying to unlink the user from does not exist");
        }

        if (chatLinkOpt.isPresent()) {
            chatLinkRepository.deleteChatLink(chatLinkOpt.get());
            boolean linkNotTrackedByChat = chatRepository
                .untrackLink(chatLinkOpt.get().getChat(), chatLinkOpt.get());
            boolean userHasBeenUnlinked = linkRepository
                .deleteTrackingChat(chatLinkOpt.get().getLink(), chatLinkOpt.get());

            if (!linkNotTrackedByChat) {
                throw new LinkNotTrackedException("The link was not tracked from the chat side");
            }

            if (!userHasBeenUnlinked) {
                throw new LinkNotTrackedException("The chat was not pinned from the link side");
            }

        } else {
            throw new LinkNotTrackedException("The link was not tracked");
        }

        return chatLinkOpt.get();
    }

}
