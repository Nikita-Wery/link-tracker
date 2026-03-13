package backend.academy.linktracker.bot.dialog.trackdialog.actions;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.StateAction;
import backend.academy.linktracker.bot.utils.validator.LinkValidationProcessor;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class UrlAction implements StateAction {

    private static final String STATE_MESSAGE = "Отправьте необходимые вам теги";
    private static final String INVALID_LINK_MESSAGE
        = "Такая ссылка на данный момент не поддерживается(";

    private final TelegramMessageSender telegramMessageSender;
    private final LinkValidationProcessor linkValidator;

    public UrlAction(
            TelegramMessageSender telegramMessageSender,
            LinkValidationProcessor linkValidator) {

        this.telegramMessageSender = telegramMessageSender;
        this.linkValidator = linkValidator;
    }

    @Override
    public boolean execute(Update update, DialogContext context) {

        if (linkValidator.isValid(update.message().text().trim())) {
            context.setUrl(URI.create(update.message().text()));
            telegramMessageSender.sendMessage(update.message().chat().id(), STATE_MESSAGE);
            return true;
        } else {
            telegramMessageSender.sendMessage(update.message().chat().id(), INVALID_LINK_MESSAGE);
            return false;
        }

    }
}
