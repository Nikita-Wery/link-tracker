package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/list";
    public static final String COMMAND_DESCRIPTION ="Вывести список всех ссылок, отслеживаемых пользователем";

    private static final String NEVER_ATTACHED_LINK_MESSAGE
        = "Чтобы получить список отслеживаемых ссылок, начните отслеживать хотя бы одну";
    private static final String NO_TRACKED_LINKS_MESSAGE
        = "Сейчас вы не отслеживаете ни одной ссылки";

    private final ScrapperClient client;
    private final TelegramMessageSender sender;

    public ListCommand(ScrapperClient client, TelegramMessageSender sender) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.client = client;
        this.sender = sender;
    }

    @Override
    public void handle(Update update) {
        try {
            ListLinksResponse listLinksResponse = client.getLinks(update.message().chat().id());

            if (listLinksResponse.size() == 0) {
                sender.sendMessage(update.message().chat().id(), NO_TRACKED_LINKS_MESSAGE);
            } else {
                // TODO: доделать получение тэгов и т.д.
//                List<LinkResponse> linkResponses =
            }

        } catch (ChatNotExistsException ex) {
            log.info("The user tried to get chats, but he didn't attach any", ex);
            sender.sendMessage(update.message().chat().id(), NEVER_ATTACHED_LINK_MESSAGE);
        }
    }
}
