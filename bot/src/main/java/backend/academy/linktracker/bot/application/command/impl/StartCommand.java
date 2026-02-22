package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class StartCommand extends AbstractCommand<Update> {

    private final TelegramMessageSender telegramMessageSender;

    public StartCommand(TelegramMessageSender telegramMessageSender) {
        super("/start", "Бот запускается");
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        telegramMessageSender.sendMessage(
            chatId,
            getCommandDescription()
        );
    }

}
