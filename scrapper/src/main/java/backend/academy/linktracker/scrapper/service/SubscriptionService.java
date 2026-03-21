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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SubscriptionService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final ChatLinkRepository chatLinkRepository;

    public SubscriptionService(
            ChatRepository chatRepository, LinkRepository linkRepository, ChatLinkRepository chatLinkRepository) {

        this.chatRepository = chatRepository;
        this.linkRepository = linkRepository;
        this.chatLinkRepository = chatLinkRepository;
    }

    public ChatLink trackLink(Chat chat, Link link, Set<String> filters, Set<String> tags) {

        Optional<Chat> chatOptional = chatRepository.findChatByChatId(chat.getChatId());
        Optional<Link> linkOptional = linkRepository.findLinkByURI(link.getUrl());

        ChatLink chatLink = ChatLink.builder()
                .chat(chatOptional.orElse(chat))
                .link(linkOptional.orElse(link))
                .filters(filters)
                .tags(tags)
                .build();

        chatLinkRepository.findChatLinkById(chatLink.getBusinessId()).ifPresent(cl -> {
            throw new LinkAlreadyTrackedException("The link is already being tracked by the chat");
        });

        if (chatOptional.isEmpty()) chatRepository.save(chat);

        if (linkOptional.isEmpty()) linkRepository.save(link);

        return chatLinkRepository.save(chatLink);
    }

    public ChatLink untrackLink(Chat chat, Link link) {

        Chat chatEntity = chatRepository
                .findChatByChatId(chat.getChatId())
                .orElseThrow(() ->
                        new ChatNotExistsException("The chat you are trying to unpin the link from does not exist"));

        Link linkEntity = linkRepository
                .findLinkByURI(link.getUrl())
                .orElseThrow(() ->
                        new LinkNotExistsException("The link you are trying to unlink the user from does not exist"));

        ChatLink.BusinessId chatLinkId = new ChatLink.BusinessId(linkEntity.getId(), chatEntity.getId());

        ChatLink chatLink = chatLinkRepository
                .findChatLinkById(chatLinkId)
                .orElseThrow(() -> new LinkNotTrackedException("The link was not tracked"));

        chatLinkRepository.deleteChatLink(chatLink);

        boolean linkNotTrackedByChat = chatRepository.untrackLink(chatEntity, chatLink);

        boolean userHasBeenUnlinked = linkRepository.deleteTrackingChat(linkEntity, chatLink);

        if (!linkNotTrackedByChat) {
            throw new LinkNotTrackedException("The link was not tracked from the chat side");
        }

        if (!userHasBeenUnlinked) {
            throw new LinkNotTrackedException("The chat was not pinned from the link side");
        }

        return chatLink;
    }

    public List<ChatLink> getTrackedLinksByChatId(Long chatId) {
        Chat chat =
                chatRepository.findChatByChatId(chatId).orElseThrow(() -> new ChatNotExistsException("Chat not found"));

        return chat.getTrackedLinks().stream().toList();
    }
}
