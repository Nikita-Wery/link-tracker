package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.springframework.stereotype.Repository;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryChatRepository implements ChatRepository {

    private final Set<Chat> chatRepository;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public InMemoryChatRepository() {
        this.chatRepository = new HashSet<>();
    }

    @Override
    public Chat save(Chat chat) {
        chat.setId(idGenerator.getAndIncrement());
        chatRepository.add(chat);

        return chat;
    }

    @Override
    public void deleteByChatId(long chatId) {
        Iterator<Chat> iterator = chatRepository.iterator();

        while (iterator.hasNext()) {
            Chat chat = iterator.next();

            if (chat.getChatId() == chatId) {
                iterator.remove();
            }
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
    public boolean untrackLink(Chat chat, ChatLink chatLink) {
        boolean linkUnpinned = false;
        Iterator<ChatLink> iterator = chat.getTrackedLinks().iterator();

        while (iterator.hasNext()) {
            ChatLink chatLinkNow = iterator.next();

            if (chatLinkNow.equals(chatLink)) {
                iterator.remove();
                linkUnpinned = true;
            }
        }

        return linkUnpinned;
    }

}
