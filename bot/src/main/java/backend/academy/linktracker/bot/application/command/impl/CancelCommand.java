package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class CancelCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/cancel";
    public static final String COMMAND_DESCRIPTION = "Прервать выполнение текущего диалога";

    private static final String COMMAND_MESSAGE = "Текущее действие успешно прервано";

    private final TelegramMessageSender sender;

    public CancelCommand(TelegramMessageSender sender) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.sender = sender;
    }

    @Override
    public void handle(Update update) {
        sender.sendMessage(update.message().chat().id(), COMMAND_MESSAGE);
    }
}
