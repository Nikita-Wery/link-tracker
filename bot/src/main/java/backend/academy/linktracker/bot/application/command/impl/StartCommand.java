package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
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
    private static final String COMMAND_DESCRIPTION = "Готовы пообщаться?)";

    private final TelegramMessageSender telegramMessageSender;

    public StartCommand(TelegramMessageSender telegramMessageSender) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.telegramMessageSender = telegramMessageSender;
    }

    /**
     * Выводит приветственное сообщение пользователю
     * * @param data данные, необходимые для отправки сообщения
     */
    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        telegramMessageSender.sendMessage(chatId, getCommandDescription());
    }
}
