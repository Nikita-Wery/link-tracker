package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.application.command.Command;
import com.pengrad.telegrambot.model.Update;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends AbstractCommand<Update> {

    private final TelegramMessageSender telegramMessageSender;
    private final List<Command> commands;

    public HelpCommand(TelegramMessageSender telegramMessageSender, List<Command> commands) {
        super("/help", "Список доступных команд");
        // необходимо из-за циклической зависимости
        commands.add(this);
        this.telegramMessageSender = telegramMessageSender;
        this.commands = commands;
    }

    @Override
    public void handle(Update data) {
        Long chatId = data.message().chat().id();

        String helpMessage = commands.stream()
                .filter(command -> !command.getCommandName().equals("/unknown"))
                .sorted(Comparator.comparing(Command::getCommandName))
                .map(cmd -> String.format("%s - %s", cmd.getCommandName(), cmd.getCommandDescription()))
                .collect(Collectors.joining("\n", "Доступные команды:\n", ""));

        telegramMessageSender.sendMessage(chatId, helpMessage);
    }
}
