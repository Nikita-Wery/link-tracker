package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class UnknownCommand extends AbstractCommand<Update> {

    private final TelegramMessageSender telegramMessageSender;

    public UnknownCommand(TelegramMessageSender telegramMessageSender) {
        super("/unknown", "Неизвестная команда. Используйте /help");
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        telegramMessageSender.sendMessage(chatId, getCommandDescription());
    }
}
