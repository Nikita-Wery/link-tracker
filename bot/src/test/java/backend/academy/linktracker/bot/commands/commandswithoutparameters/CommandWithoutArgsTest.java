package backend.academy.linktracker.bot.commands.commandswithoutparameters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.impl.HelpCommand;
import backend.academy.linktracker.bot.application.command.impl.StartCommand;
import backend.academy.linktracker.bot.application.command.impl.UnknownCommand;
import backend.academy.linktracker.bot.application.dispatcher.impl.CommandDispatcher;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.CommandValidator;
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
class CommandWithoutArgsTest {

    @Mock
    private DialogContextStorage contextStorage;

    @Mock
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

        // commands
        StartCommand startCommand = new StartCommand(telegramMessageSender);
        UnknownCommand unknownCommand = new UnknownCommand(telegramMessageSender);

        List<Command<Update>> commands = new ArrayList<>();
        commands.add(startCommand);

        HelpCommand helpCommand = new HelpCommand(telegramMessageSender, commands);

        commands.add(helpCommand);
        commands.add(unknownCommand);

        commandDispatcher = new CommandDispatcher(commands, commandValidator, contextStorage);
    }

    @AfterEach
    void verifyDialogDeletion() {
        verify(contextStorage).clearDialog(200L);
    }

    @Test
    void StartCommandTest() {
        when(message.text()).thenReturn("/start");

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq("Готовы пообщаться?)"));
    }

    @Test
    void HelpCommandTest() {
        when(message.text()).thenReturn("/help");

        commandDispatcher.dispatch(update);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(telegramMessageSender).sendMessage(eq(200L), captor.capture());

        String sentText = captor.getValue();

        assertTrue(commandDispatcher.getListOfCommands().stream()
                .filter(c -> !c.getCommandName().equals(HelpCommand.COMMAND_NAME))
                .allMatch(c -> sentText.contains(c.getCommandName())));
    }

    @Test
    void UnknownCommandTest() {
        when(message.text()).thenReturn("kakoi to bred");

        commandDispatcher.dispatch(update);

        verify(telegramMessageSender).sendMessage(eq(200L), eq("Неизвестная команда. Используйте /help"));
    }
}
