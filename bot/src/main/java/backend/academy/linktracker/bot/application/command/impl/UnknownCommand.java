package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

/**
 * Команда выводящаяся при неизвестном
 * действии пользователя
 *
 * !НЕ ВХОДИТ В СПИСОК КОМАНД ДОСТУПНЫХ ПОЛЬЗОВАТЕЛЮ
 *
 * @author Luzin Nikita
 */
@Component
public class UnknownCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/unknown";
    public static final String COMMAND_DESCRIPTION = "Неизвестная команда. Используйте /help";

    private final TelegramMessageSender telegramMessageSender;
    private final BotMetricsService botMetrics;

    public UnknownCommand(TelegramMessageSender telegramMessageSender, BotMetricsService botMetrics) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.telegramMessageSender = telegramMessageSender;
        this.botMetrics = botMetrics;
    }

    /**
     * Выводит сообщение о незарегестрированной команде
     *
     * @param data - данные, необходимые для отправки сообщения
     */
    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        telegramMessageSender.sendMessage(chatId, getCommandDescription());
        botMetrics.incrementCommand(COMMAND_NAME);
    }
}
