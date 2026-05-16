package backend.academy.linktracker.bot.commands.commandswithoutparameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.impl.CancelCommand;
import backend.academy.linktracker.bot.application.command.impl.TrackCommand;
import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
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
class TrackCommandTest {

    private static final String COMMAND_TEXT =
            "Отправьте ссылку на ресурс, который хотите отслеживать%n%n%s - чтобы прервать выполнение"
                    .formatted(CancelCommand.COMMAND_NAME);

    @Mock
    private TelegramMessageSender telegramMessageSender;

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private DialogContextStorage contextStorage;

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

        TrackCommand trackCommand = new TrackCommand(telegramMessageSender, contextStorage);

        commands.add(trackCommand);

        commandDispatcher = new CommandDispatcher(commands, null, contextStorage);
    }

    @AfterEach
    void verifyDialogDeletion() {
        verify(contextStorage).clearDialog(200L);
    }

    @Test
    void TrackCommandTest() {
        when(message.text()).thenReturn("/track");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(telegramMessageSender).sendMessage(eq(200L), messageCaptor.capture());

        ArgumentCaptor<DialogContext> contextCaptor = ArgumentCaptor.forClass(DialogContext.class);

        verify(contextStorage).save(eq(200L), contextCaptor.capture());

        assertEquals(TrackingDialogStates.WAITING_URL, contextCaptor.getValue().getState());
        assertEquals(COMMAND_TEXT, messageCaptor.getValue());
    }
}
