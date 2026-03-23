package backend.academy.linktracker.bot.commands.commandswithparameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.configuration.telgramconfiguration.TelegramTestConfiguration;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import java.util.List;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TelegramTestConfiguration.class)
public class ListCommandIntegrationTests {

    private static final String NEVER_ATTACHED_LINK_MESSAGE =
            "Чтобы получить список отслеживаемых ссылок, начните отслеживать хотя бы одну";
    private static final String NO_TRACKED_LINKS_MESSAGE = "Сейчас вы не отслеживаете ни одной ссылки";
    public static final String ILLEGAL_TEG_MESSAGE = "Тэги введённые вами не могут существовать";

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private ScrapperClient scrapperClient;

    @MockitoBean
    private DialogContextStorage contextStorage;

    @Autowired
    private CommandDispatcher commandDispatcher;

    //    @Autowired
    //    private ObjectMapper objectMapper;

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
    @DisplayName("Сценарий: пользователь впервые пользуется ботом")
    public void execute_FirstUse() {
        when(message.text()).thenReturn("/list");
        when(scrapperClient.getLinks(chat.id())).thenThrow(new ChatNotExistsException("chat not exists"));

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(NEVER_ATTACHED_LINK_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: у пользователя нет отслеживаемых ссылок")
    public void execute_UserHasNoTrackedLinks() {
        ListLinksResponse response = mock(ListLinksResponse.class);
        when(message.text()).thenReturn("/list");
        when(response.size()).thenReturn(0);
        when(scrapperClient.getLinks(chat.id())).thenReturn(response);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(NO_TRACKED_LINKS_MESSAGE, sentMessage.getText());
    }

    @Test
    @DisplayName("Сценарий: пользователь задал валидные тэги")
    public void execute_UserHasLinks_UserSetTags() {

        List<LinkResponse> listOfResponses = List.of(
                new LinkResponse(1L, URI.create("https://firstLink"), List.of("first")),
                new LinkResponse(2L, URI.create("https://secondLink"), List.of("second")),
                new LinkResponse(3L, URI.create("https://thirdLink"), List.of("second", "first")));

        ListLinksResponse response = new ListLinksResponse(listOfResponses, listOfResponses.size());

        when(message.text()).thenReturn("/list first");
        when(scrapperClient.getLinks(chat.id())).thenReturn(response);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertTrue(sentMessage.getText().contains("https://firstLink")
                && sentMessage.getText().contains("https://thirdLink")
                && !sentMessage.getText().contains("https://secondLink"));
    }

    @Test
    @DisplayName("Сценарий: пользователь ввёл невалидные тэги")
    public void execute_IllegalTags() {
        ListLinksResponse response = mock(ListLinksResponse.class);
        when(response.size()).thenReturn(3);
        when(message.text()).thenReturn("/list ?");
        when(scrapperClient.getLinks(chat.id())).thenReturn(response);

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(ILLEGAL_TEG_MESSAGE, sentMessage.getText());
    }
}
