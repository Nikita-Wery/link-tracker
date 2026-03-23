package backend.academy.linktracker.bot.application.bootstrap;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.impl.UnknownCommand;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Регистратор комманд
 *
 * @author Luzin Nikita
 */
@Component
public class TelegramCommandRegistrar {

    private final TelegramBot telegramBot;
    private final List<Command<Update>> commands;

    public TelegramCommandRegistrar(TelegramBot telegramBot, List<Command<Update>> commands) {
        this.telegramBot = telegramBot;
        this.commands = commands;
    }

    /**
     * Регестрирует доступные команды в telegram боте
     * используется TelegramAPI
     */
    public void registerCommands() {
        List<BotCommand> botCommands = commands.stream()
                .filter(cmd -> !cmd.getCommandName().equals(UnknownCommand.COMMAND_NAME))
                .map(cmd -> new BotCommand(cmd.getCommandName(), cmd.getCommandDescription()))
                .toList();

        telegramBot.execute(new SetMyCommands(botCommands.toArray(new BotCommand[0])));
    }
}
