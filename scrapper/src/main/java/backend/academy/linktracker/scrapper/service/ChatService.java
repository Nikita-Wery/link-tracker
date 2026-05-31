package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final ScrapperMetricsService scrapperMetricsService;

    public ChatService(ChatRepository chatRepository, ScrapperMetricsService scrapperMetricsService) {
        this.chatRepository = chatRepository;
        this.scrapperMetricsService = scrapperMetricsService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public Chat addChat(Chat chat) {

        try {

            return scrapperMetricsService.timeExternalCall(
                    "database", "addChat", "chatservice", () -> chatRepository.saveAndFlush(chat));

        } catch (DataIntegrityViolationException ex) {
            log.warn("Chat already exists", kv("chat_id", chat.getChatId()));
            throw new ChatAlreadyExistsException("Chat already exists");
        }
    }

    @Transactional
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void deleteChatById(long chatId) {

        int deleted = scrapperMetricsService.timeExternalCall(
                "database", "deleteChatById", "chatservice", () -> chatRepository.deleteByChatId(chatId));

        if (deleted == 0) {
            log.warn("Chat not exists", kv("chat_id", chatId));
            throw new ChatNotExistsException("Chat: " + chatId + "not found for deletion");
        }
    }
}
