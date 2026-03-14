package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class TrackCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/track";
    public static final String COMMAND_DESCRIPTION = "Начать отслеживание ссылки";

    private static final String COMMAND_TEXT =
            "Отправьте ссылку на ресурс, который хотите отслеживать%n%n%s - чтобы прервать выполнение"
                    .formatted(CancelCommand.COMMAND_NAME);

    private final TelegramMessageSender sender;
    private final DialogContextStorage contextStorage;

    public TrackCommand(TelegramMessageSender sender, DialogContextStorage contextStorage) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.contextStorage = contextStorage;
        this.sender = sender;
    }

    @Override
    public void handle(Update update) {
        sender.sendMessage(update.message().chat().id(), COMMAND_TEXT);

        contextStorage.save(update.message().chat().id(), new DialogContext(TrackingDialogStates.WAITING_URL));
    }
}
