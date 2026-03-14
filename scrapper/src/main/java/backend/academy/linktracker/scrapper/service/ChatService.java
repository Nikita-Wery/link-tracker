package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat addChat(Chat chat) {
        Optional<Chat> chatOptional = chatRepository.findChatByChatId(chat.getChatId());

        if (chatOptional.isEmpty()) {
            throw new LinkAlreadyTrackedException("The link is already being tracked by the chat");
        }

        return chatRepository.save(chat);
    }

    public void deleteChatById(long chatId) {
        Optional<Chat> chatOptional = chatRepository.findChatByChatId(chatId);

        if (chatOptional.isEmpty()) {
            throw new ChatNotExistsException("Chat: " + chatId + "not found for deletion");
        }

        chatRepository.deleteByChatId(chatId);
    }

    public Optional<Chat> getChatById(long chatId) {
        return chatRepository.findChatByChatId(chatId);
    }
}
