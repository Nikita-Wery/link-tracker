package backend.academy.linktracker.bot.commands.commandswithoutparameters;

import backend.academy.linktracker.bot.application.command.impl.CancelCommand;
import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.configuration.telgramconfiguration.TelegramTestConfiguration;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TelegramTestConfiguration.class)
public class TrackCommandIntegrationTests {

    private static final String COMMAND_TEXT =
        "Отправьте ссылку на ресурс, который хотите отслеживать%n%n%s - чтобы прервать выполнение"
            .formatted(CancelCommand.COMMAND_NAME);

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private ScrapperClient scrapperClient;

    @MockitoBean
    private DialogContextStorage contextStorage;

    @Autowired
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
    }

    @AfterEach()
    public void verifyDialogDeletion() {
        verify(contextStorage).clearDialog(eq(200L));
    }

    @Test
    @DisplayName("Сценарий: после команд /track, пользователю предлагается ввести ссылку")
    public void execute_FirstUse() {
        when(message.text()).thenReturn("/track");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        ArgumentCaptor<DialogContext> dialogContext = ArgumentCaptor.forClass(
            DialogContext.class
        );
        verify(contextStorage).save(eq(200L), dialogContext.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        DialogContext context = dialogContext.getValue();
        assertEquals(TrackingDialogStates.WAITING_URL, context.getState());

        assertEquals(COMMAND_TEXT, sentMessage.getText());
    }

}
