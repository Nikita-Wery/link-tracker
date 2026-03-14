package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import org.springframework.stereotype.Service;

@Service
public class LinkUpdateService {

    private static final String DEFAULT_UPDATE_DESCRIPTION = "Ссылка: %s, обновилась";
    private final TelegramMessageSender telegramMessageSender;

    public LinkUpdateService(TelegramMessageSender telegramMessageSender) {
        this.telegramMessageSender = telegramMessageSender;
    }

    public void sendUpdateMessage(LinkUpdate linkUpdate) {

        for (Long chatId : linkUpdate.tgChatIds()) {

            if (linkUpdate.description() == null || linkUpdate.description().isBlank()) {
                telegramMessageSender.sendMessage(chatId, DEFAULT_UPDATE_DESCRIPTION.formatted(linkUpdate.url()));
            } else {
                telegramMessageSender.sendMessage(chatId, linkUpdate.description());
            }
        }
    }
}
