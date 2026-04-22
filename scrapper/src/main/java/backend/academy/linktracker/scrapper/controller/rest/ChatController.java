package backend.academy.linktracker.scrapper.controller.rest;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tg-chat")
public class ChatController {

    private final ChatService chatService;
    private final DtoEntityMapper dtoEntityMapper;

    public ChatController(ChatService chatService, DtoEntityMapper dtoEntityMapper) {
        this.chatService = chatService;
        this.dtoEntityMapper = dtoEntityMapper;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @PostMapping("/{id}")
    public void registerChat(@PathVariable("id") Long chatId) {
        log.info("chat_registration", kv("chat_id", chatId));

        chatService.addChat(dtoEntityMapper.getChatFromChatId(chatId));
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable("id") Long chatId) {
        log.info("deleting_chat", kv("chat_id", chatId));

        chatService.deleteChatById(chatId);
    }
}
