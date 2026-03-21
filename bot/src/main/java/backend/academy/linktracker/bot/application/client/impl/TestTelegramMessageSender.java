package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "false")
public class TestTelegramMessageSender implements TelegramMessageSender {

    @Override
    public void sendMessage(Long chatId, String message) {}
}
