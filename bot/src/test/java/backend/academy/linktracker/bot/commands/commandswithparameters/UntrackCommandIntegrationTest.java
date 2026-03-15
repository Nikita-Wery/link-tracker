package backend.academy.linktracker.bot.commands.commandswithparameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.configuration.telgramconfiguration.TelegramTestConfiguration;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.LinkNotTrackedException;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.LinkValidationProcessor;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TelegramTestConfiguration.class)
public class UntrackCommandIntegrationTest {

    private static final String MISMATCH_NUMBER_OF_ARGS_MESSAGE =
            "За один раз можно открепить только одну ссылку, ни больше ни меньше";
    private static final String INVALID_LINK_MESSAGE = "Извините, но в данный момент такая ссылка не поддерживается";
    private static final String SUCCESSFUL_LINK_UNPINNING_MESSAGE =
            "Ссылка успешно откреплена и больше не отслеживается";
    private static final String USER_HAS_NEVER_ATTACHED_LINK_MESSAGE =
            "Чтобы открепить ссылку, начните отслеживать хотя бы одну";
    private static final String USER_NOT_CURRENTLY_FOLLOWING_ANY_LINKS_MESSAGE =
            "В данный момент вы не отслеживаете ни одной ссылки";

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private ScrapperClient scrapperClient;

    @MockitoSpyBean
    private LinkValidationProcessor validator;

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
    @DisplayName("Сценарий: ползьователь добавил что-то кроме ссылки")
    public void execute_ExcessOfArguments() {
        when(message.text()).thenReturn("/untrack link1 link2");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(MISMATCH_NUMBER_OF_ARGS_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: пользоватль не добавил ссылку")
    public void execute_NoLink() {
        when(message.text()).thenReturn("/untrack");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(MISMATCH_NUMBER_OF_ARGS_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: пользователь ни разу не прикреплял ссылку ранее")
    public void execute_FirstUse() {
        String link = "link";
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link);
        when(message.text()).thenReturn("/untrack " + link);
        when(validator.isValid(link)).thenReturn(true);
        when(scrapperClient.untrackLink(chat.id(), removeLinkRequest)).thenThrow(ChatNotExistsException.class);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(USER_HAS_NEVER_ATTACHED_LINK_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: в данный момент пользователь не отслеживает ни одной ссылки")
    public void execute_NoTrackingLink() {
        String link = "link";
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link);
        when(validator.isValid(link)).thenReturn(true);
        when(message.text()).thenReturn("/untrack " + link);
        when(scrapperClient.untrackLink(chat.id(), removeLinkRequest)).thenThrow(LinkNotTrackedException.class);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(USER_NOT_CURRENTLY_FOLLOWING_ANY_LINKS_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: ссылка невалидна")
    public void execute_InvalidLink() {
        when(message.text()).thenReturn("/untrack link");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(INVALID_LINK_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: открепление успешно")
    public void execute_Success() {
        LinkResponse response = mock(LinkResponse.class);
        String link = "https://github.com/user/repo";
        when(message.text()).thenReturn("/untrack " + link);
        when(scrapperClient.untrackLink(eq(chat.id()), any(RemoveLinkRequest.class)))
                .thenReturn(response);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(SUCCESSFUL_LINK_UNPINNING_MESSAGE, sentMessage.getText());
    }
}
