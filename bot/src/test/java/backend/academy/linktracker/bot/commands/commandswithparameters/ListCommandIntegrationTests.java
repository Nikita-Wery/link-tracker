package backend.academy.linktracker.bot.commands.commandswithparameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.impl.ListCommand;
import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.CommandValidator;
import backend.academy.linktracker.bot.utils.validator.TagsValidator;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListCommandTest {

    private static final String NEVER_ATTACHED_LINK_MESSAGE =
            "Чтобы получить список отслеживаемых ссылок, начните отслеживать хотя бы одну";
    private static final String NO_TRACKED_LINKS_MESSAGE = "Сейчас вы не отслеживаете ни одной ссылки";
    public static final String ILLEGAL_TEG_MESSAGE = "Тэги введённые вами не могут существовать";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private DialogContextStorage contextStorage;

    @Mock
    private CommandValidator commandValidator;

    @Mock
    private TagsValidator tagsValidator;

    @Mock
    private TelegramMessageSender telegramMessageSender;

    private CommandDispatcher commandDispatcher;

    private Update update;
    private Message message;
    private Chat chat;

    @BeforeEach
    void setUp() {
        update = mock(Update.class);
        message = mock(Message.class);
        chat = mock(Chat.class);

        when(chat.id()).thenReturn(200L);
        when(message.chat()).thenReturn(chat);
        when(update.message()).thenReturn(message);

        List<Command<Update>> commands = new ArrayList<>();

        ListCommand listCommand = new ListCommand(scrapperClient, telegramMessageSender, tagsValidator);

        commands.add(listCommand);

        commandDispatcher = new CommandDispatcher(commands, commandValidator, contextStorage);
    }

    @AfterEach
    void verifyDialogDeletion() {
        verify(contextStorage).clearDialog(200L);
    }

    @Test
    void execute_FirstUse() {
        when(message.text()).thenReturn("/list");
        when(scrapperClient.getLinks(chat.id())).thenThrow(new ChatNotExistsException("chat not exists"));

        commandDispatcher.dispatch(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(telegramMessageSender).sendMessage(eq(200L), textCaptor.capture());

        assertEquals(NEVER_ATTACHED_LINK_MESSAGE, textCaptor.getValue());
    }

    @Test
    void execute_UserHasNoTrackedLinks() {
        ListLinksResponse response = mock(ListLinksResponse.class);

        when(message.text()).thenReturn("/list");
        when(response.size()).thenReturn(0);
        when(scrapperClient.getLinks(chat.id())).thenReturn(response);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(telegramMessageSender).sendMessage(eq(200L), textCaptor.capture());

        assertEquals(NO_TRACKED_LINKS_MESSAGE, textCaptor.getValue());
    }

    @Test
    void execute_IllegalTags() {
        ListLinksResponse response = mock(ListLinksResponse.class);

        when(response.size()).thenReturn(3);
        when(message.text()).thenReturn("/list ?");
        when(scrapperClient.getLinks(chat.id())).thenReturn(response);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(telegramMessageSender).sendMessage(eq(200L), textCaptor.capture());

        assertEquals(ILLEGAL_TEG_MESSAGE, textCaptor.getValue());
    }
}
