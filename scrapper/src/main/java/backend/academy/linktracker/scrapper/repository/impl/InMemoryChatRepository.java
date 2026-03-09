package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistException;
import backend.academy.linktracker.scrapper.exception.ChatNotExistException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.exception.LinkNotTrackedException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public class InMemoryChatRepository implements ChatRepository {

    private Set<Chat> chatRepository;

    public InMemoryChatRepository() {
        this.chatRepository = new HashSet<>();
    }

    @Override
    public Chat save(Chat chat) {
        boolean added = chatRepository.add(chat);

        if (!added) {
            throw new ChatAlreadyExistException("Chat: " + chat.getChatId() + "already exists");
        }

        return chat;
    }

    @Override
    public void deleteById(long chatId) {
        boolean isDeleted = false;
        Iterator<Chat> iterator = chatRepository.iterator();

        while (iterator.hasNext()) {
            Chat chat = iterator.next();

            if (chat.getChatId() == chatId) {
                iterator.remove();
                isDeleted = true;
            }
        }

        if (!isDeleted) {
            throw new ChatNotExistException("Chat: " + chatId + "not found for deletion");
        }
    }

    @Override
    public Optional<Chat> findChatById(long chatId) {
        Optional<Chat> result = Optional.empty();

        for (Chat chat : chatRepository) {
            if (chat.getChatId() == chatId) result = Optional.of(chat);
        }

        return result;
    }

    @Override
    public Link addLinkToChat(Link link, long chatId) {
        Optional<Chat> chatOptional = findChatById(chatId);

        if (chatOptional.isPresent()) {
            boolean added = chatOptional.get().addLink(link);

            if (!added) {
                throw new LinkAlreadyTrackedException("The link is already being tracked by the chat");
            }

        } else {
            throw new ChatNotExistException("The chat we want to add a link to does not exist");
        }

        return link;
    }

    @Override
    public void deleteLinkFromChat(Link link, long chatId) {
        boolean deleted = false;
        Optional<Chat> chatOptional = findChatById(chatId);

        if (chatOptional.isPresent()) {
            Iterator<Chat> iterator = chatRepository.iterator();

            while (iterator.hasNext()) {
                Chat chat = iterator.next();

                if (chat.getChatId() == chatId) {
                    chat.untrackLink(link);
                    deleted = true;
                }
            }

            if (!deleted) {
                throw new LinkNotTrackedException("The link is not trackable.");
            }

        } else {
            throw new ChatNotExistException("The chat we want to add a link to does not exist");
        }

    }


}
