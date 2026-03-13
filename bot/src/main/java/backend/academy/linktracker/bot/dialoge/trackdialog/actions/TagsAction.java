package backend.academy.linktracker.bot.dialoge.trackdialog.actions;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.dialoge.DialogContext;
import backend.academy.linktracker.bot.dialoge.StateAction;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.service.LinkService;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class TagsAction implements StateAction {

    private static final String DEFAULT_SUCCESS_MESSAGE = "Ссылка сохранена";

    private final LinkService linkService;
    private final DialogContextStorage storage;
    private final TelegramMessageSender sender;

    public TagsAction(LinkService linkService,
                      DialogContextStorage storage,
                      TelegramMessageSender sender) {
        this.linkService = linkService;
        this.storage = storage;
        this.sender = sender;
    }

    @Override
    public void execute(Update update, DialogContext context) {
        // TODO: сделать валидацию тэгов
        context.setFilters(Set.of(update.message().text().split(",")));

        // TODO: обработать excepitons
        try {
            linkService.save(
                update.message().chat().id(),
                context.getUrl(),
                context.getTags()
            );
        } catch (Exception ex) {

        }

        storage.clearDialog(update.message().chat().id());

        sender.sendMessage(update.message().chat().id(), DEFAULT_SUCCESS_MESSAGE);
    }
}
