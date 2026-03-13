package backend.academy.linktracker.bot.dialoge.trackdialog.actions;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.dialoge.DialogContext;
import backend.academy.linktracker.bot.dialoge.StateAction;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class UrlAction implements StateAction {

    private static final String STATE_MESSAGE = "Отправьте необходимые вам теги";

    private final TelegramMessageSender telegramMessageSender;

    public UrlAction(TelegramMessageSender telegramMessageSender) {
        this.telegramMessageSender = telegramMessageSender;
    }

    @Override
    public void execute(Update update, DialogContext context) {
        // TODO: сделать валидацию
        context.setUrl(URI.create(update.message().text()));
        telegramMessageSender.sendMessage(update.message().chat().id(), STATE_MESSAGE);
    }
}
