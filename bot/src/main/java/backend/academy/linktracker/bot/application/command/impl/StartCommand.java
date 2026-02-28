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

    private final TelegramMessageSender telegramMessageSender;

    public StartCommand(TelegramMessageSender telegramMessageSender) {
        super("/start", "Готовы пообщаться?)");
        this.telegramMessageSender = telegramMessageSender;
    }

    /**
     * Выводит приветственное сообщение пользователю
     *
     * @param data данные, необходимые для отправки сообщения
     */
    @Override
    public void handle(Update data) {
        String chatId = String.valueOf(data.message().chat().id());

        telegramMessageSender.sendMessage(chatId, getCommandDescription());
    }
}
