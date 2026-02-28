package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
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

    private final TelegramMessageSender telegramMessageSender;

    public UnknownCommand(TelegramMessageSender telegramMessageSender) {
        super("/unknown", "Неизвестная команда. Используйте /help");
        this.telegramMessageSender = telegramMessageSender;
    }

    /**
     * Выводит сообщение о незарегестрированной команде
     *
     * @param data - данные, необходимые для отправки сообщения
     */
    @Override
    public void handle(Update data) {
        String chatId = String.valueOf(data.message().chat().id());

        telegramMessageSender.sendMessage(chatId, getCommandDescription());
    }
}
