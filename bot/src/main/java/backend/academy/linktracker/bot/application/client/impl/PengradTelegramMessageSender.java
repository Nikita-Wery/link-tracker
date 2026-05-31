package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Реализует отправку сообщений используя
 * <a href="https://github.com/pengrad/java-telegram-bot-api">Pengrad</a>
 *
 * @author Luzin Nikita
 */
@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class PengradTelegramMessageSender implements TelegramMessageSender {

    private final TelegramBot telegramBot;
    private final BotMetricsService botMetrics;

    @Autowired
    public PengradTelegramMessageSender(TelegramBot telegramBot, BotMetricsService botMetrics) {
        this.telegramBot = telegramBot;
        this.botMetrics = botMetrics;
    }

    @Override
    public void sendMessage(Long chatId, String message) {
        telegramBot.execute(new SendMessage(chatId, message));
        botMetrics.incrementSentNotifications();
    }
}
