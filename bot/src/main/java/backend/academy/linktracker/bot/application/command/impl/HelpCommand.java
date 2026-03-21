package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.application.command.Command;
import com.pengrad.telegrambot.model.Update;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Команда бота {@code /help}
 *
 * @author Luzin Nikita
 */
@Component
public class HelpCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/help";
    private static final String COMMAND_DESCRIPTION = "Список доступных команд";

    private final TelegramMessageSender telegramMessageSender;
    private final List<Command<Update>> commands;

    public HelpCommand(TelegramMessageSender telegramMessageSender, List<Command<Update>> commands) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.telegramMessageSender = telegramMessageSender;
        this.commands = commands;
    }

    /**
     * Посылает список всех доступных команд
     * с пояснением к каждой
     *
     * @param data данные, необходимые для отправки сообщения
     */
    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        String helpMessage = commands.stream()
                .filter(command -> !command.getCommandName().equals(UnknownCommand.COMMAND_NAME))
                .sorted(Comparator.comparing(Command::getCommandName))
                .map(cmd -> String.format("%s - %s", cmd.getCommandName(), cmd.getCommandDescription()))
                .collect(Collectors.joining("\n", "Доступные команды:\n", ""));

        telegramMessageSender.sendMessage(chatId, helpMessage);
    }
}
