package backend.academy.linktracker.bot.commands.commandswithparameters;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.impl.UntrackCommand;
import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.LinkNotTrackedException;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.CommandValidator;
import backend.academy.linktracker.bot.utils.validator.LinkValidationProcessor;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UntrackCommandTest {

    private static final String MISMATCH_NUMBER_OF_ARGS_MESSAGE =
            "За один раз можно открепить только одну ссылку, ни больше ни меньше";
    private static final String INVALID_LINK_MESSAGE = "Извините, но в данный момент такая ссылка не поддерживается";
    private static final String SUCCESSFUL_LINK_UNPINNING_MESSAGE =
            "Ссылка успешно откреплена и больше не отслеживается";
    private static final String USER_HAS_NEVER_ATTACHED_LINK_MESSAGE =
            "Чтобы открепить ссылку, начните отслеживать хотя бы одну";
    private static final String USER_NOT_CURRENTLY_FOLLOWING_ANY_LINKS_MESSAGE =
            "В данный момент вы не отслеживаете ни одной ссылки";

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private LinkValidationProcessor validator;

    @Mock
    private DialogContextStorage contextStorage;

    @Mock
    private BotMetricsService botMetricsService;

    private CommandValidator commandValidator;

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

        UntrackCommand untrackCommand =
                new UntrackCommand(scrapperClient, telegramMessageSender, validator, botMetricsService);

        commands.add(untrackCommand);

        commandValidator = new CommandValidator(commands);

        commandDispatcher = new CommandDispatcher(commands, commandValidator, contextStorage);
    }

    @AfterEach
    void verifyDialogDeletion() {
        verify(contextStorage).clearDialog(200L);
    }

    @Test
    void execute_ExcessOfArguments() {
        when(message.text()).thenReturn("/untrack link1 link2");

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq(MISMATCH_NUMBER_OF_ARGS_MESSAGE));
    }

    @Test
    void execute_NoLink() {
        when(message.text()).thenReturn("/untrack");

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq(MISMATCH_NUMBER_OF_ARGS_MESSAGE));
    }

    @Test
    void execute_FirstUse() {
        String link = "link";
        RemoveLinkRequest request = new RemoveLinkRequest(link);

        when(message.text()).thenReturn("/untrack " + link);
        when(validator.isValid(link)).thenReturn(true);
        when(scrapperClient.untrackLink(chat.id(), request)).thenThrow(ChatNotExistsException.class);

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq(USER_HAS_NEVER_ATTACHED_LINK_MESSAGE));
    }

    @Test
    void execute_NoTrackingLink() {
        String link = "link";
        RemoveLinkRequest request = new RemoveLinkRequest(link);

        when(message.text()).thenReturn("/untrack " + link);
        when(validator.isValid(link)).thenReturn(true);
        when(scrapperClient.untrackLink(chat.id(), request)).thenThrow(LinkNotTrackedException.class);

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq(USER_NOT_CURRENTLY_FOLLOWING_ANY_LINKS_MESSAGE));
    }

    @Test
    void execute_InvalidLink() {
        when(message.text()).thenReturn("/untrack link");

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq(INVALID_LINK_MESSAGE));
    }
}
