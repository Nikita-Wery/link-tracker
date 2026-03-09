package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@RestController
@RequestMapping("/tg-chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/{id}")
    public void registerChat(@PathVariable("id") Long chatId) {
        log.info("chat_registration",
            kv("chat_id", chatId)
        );

        chatService.addChat(new Chat(chatId));
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable("id") Long chatId) {

        log.info("deleting_chat",
            kv("chat_id", chatId)
        );

        chatService.deleteChatById(chatId);
    }

}
