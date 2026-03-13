package backend.academy.linktracker.bot.utils.validator;

import backend.academy.linktracker.bot.application.command.Command;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CommandValidator {

    private final Set<String> commandsName = new HashSet<>();

    public CommandValidator(List<Command> allCommands) {
        for (Command command : allCommands) {
            commandsName.add(command.getCommandName());
        }
    }

    public boolean isCommand(String text) {
        return commandsName.contains(text);
    }

}
