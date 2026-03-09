package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat addChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public void deleteChatById(long chatId) {
        chatRepository.deleteById(chatId);
    }

    public Optional<Chat> getChatById(long chatId) {
        return chatRepository.findChatById(chatId);
    }

    public Link addLinkToChat(Link link, long chatId) {
        return chatRepository.addLinkToChat(link, chatId);
    }

    public void untrackLink(Link link, long chatId) {
        chatRepository.deleteLinkFromChat(link, chatId);
    }

}
