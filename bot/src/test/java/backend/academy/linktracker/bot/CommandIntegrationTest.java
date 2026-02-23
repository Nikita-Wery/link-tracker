package backend.academy.linktracker.bot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import backend.academy.linktracker.bot.configuration.TelegramTestConfiguration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TelegramTestConfiguration.class)
public class CommandIntegrationTest {

    @MockitoBean
    private TelegramBot telegramBot;

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

    @Test
    public void StartCommandTest() {
        when(message.text()).thenReturn("/start");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(200L, sentMessage.getChatId());
        assertEquals("Готовы пообщаться?)", sentMessage.getText());
    }

    @Test
    public void HelpCommandTest() {
        when(message.text()).thenReturn("/help");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(200L, sentMessage.getChatId());
        assertTrue(commandDispatcher.getListOfCommands().stream()
                .allMatch(command -> sentMessage.getText().contains(command.getCommandName())));
    }

    @Test
    public void UnknownCommandTest() {
        when(message.text()).thenReturn("kakoi to bred");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(argumentCaptor.capture());

        SendMessage sentMessage = argumentCaptor.getValue();
        assertEquals(200L, sentMessage.getChatId());
        assertEquals("Неизвестная команда. Используйте /help", sentMessage.getText());
    }
}
