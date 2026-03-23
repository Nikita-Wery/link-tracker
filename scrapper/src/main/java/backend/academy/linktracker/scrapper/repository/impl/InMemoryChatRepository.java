package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryChatRepository implements ChatRepository {

    private final Set<Chat> chatRepository;

    public InMemoryChatRepository() {
        this.chatRepository = new HashSet<>();
    }

    @Override
    public Chat save(Chat chat) {
        chat.getId();

        chatRepository.add(chat);
        return chat;
    }

    @Override
    public void deleteByChatId(long chatId) {
        chatRepository.removeIf(chat -> chat.getChatId().equals(chatId));
    }

    @Override
    public Optional<Chat> findChatByChatId(long chatId) {
        Optional<Chat> result = Optional.empty();

        for (Chat chat : chatRepository) {
            if (chat.getChatId() == chatId) return Optional.of(chat);
        }

        return result;
    }

    @Override
    public boolean untrackLink(Chat chat, ChatLink chatLink) {
        Iterator<ChatLink> iterator = chat.getTrackedLinks().iterator();

        while (iterator.hasNext()) {
            if (iterator.next().equals(chatLink)) {
                iterator.remove();
                return true;
            }
        }

        return false;
    }
}
