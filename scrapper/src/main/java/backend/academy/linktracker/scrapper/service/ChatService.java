package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public Chat addChat(Chat chat) {

        try {

            return chatRepository.saveAndFlush(chat);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Chat already exists", kv("chat_id", chat.getChatId()));
            throw new ChatAlreadyExistsException("Chat already exists");
        }
    }

    @Transactional(readOnly = true)
    public Optional<Chat> getChatById(long chatId) {
        return chatRepository.findChatByChatId(chatId);
    }

    @Transactional
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void deleteChatById(long chatId) {
        int deleted = chatRepository.deleteByChatId(chatId);

        if (deleted == 0) {
            log.warn("Chat not exists", kv("chat_id", chatId));
            throw new ChatNotExistsException("Chat: " + chatId + "not found for deletion");
        }
    }
}
