package backend.academy.linktracker.bot.utils;

import backend.academy.linktracker.bot.application.command.Command;
import com.pengrad.telegrambot.model.Update;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CommandValidator {

    private final Set<String> commandsName = new HashSet<>();

    public CommandValidator(List<Command<Update>> allCommands) {
        for (Command<Update> command : allCommands) {
            commandsName.add(command.getCommandName());
        }
    }

    public boolean isCommand(String text) {
        return commandsName.contains(text);
    }
}
