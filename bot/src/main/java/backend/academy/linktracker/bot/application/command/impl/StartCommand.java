package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

/**
 * Приветственная команда бота {@code /start}
 *
 * @author Luzin Nikita
 */
@Component
public class StartCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/start";
    public static final String COMMAND_DESCRIPTION = "Готовы пообщаться?)";

    private final TelegramMessageSender telegramMessageSender;
    private final BotMetricsService botMetrics;

    public StartCommand(TelegramMessageSender telegramMessageSender, BotMetricsService botMetrics) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.telegramMessageSender = telegramMessageSender;
        this.botMetrics = botMetrics;
    }

    /**
     * Выводит приветственное сообщение пользователю
     *
     * @param data данные, необходимые для отправки сообщения
     */
    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        telegramMessageSender.sendMessage(chatId, getCommandDescription());
        botMetrics.incrementCommand(COMMAND_NAME);
    }
}
