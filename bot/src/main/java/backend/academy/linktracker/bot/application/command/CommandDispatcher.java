package backend.academy.linktracker.bot.application.command;

import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher {

    private final Logger log = LogManager.getLogger(CommandDispatcher.class);

    private Map<String, Command> commands = new HashMap<>();

    public CommandDispatcher(List<Command> commands) {
        commands.stream().forEach(command -> this.commands.put(command.getCommandName(), command));
    }

    public void register(String commandName, Command command) {
        this.commands.put(commandName, command);
    }

    @PostConstruct
    public void logCommands() {
        log.info("Commands loaded in Dispatcher:");
        commands.keySet().forEach(cmd -> log.info(" - {}", cmd));
    }

    public List<Command> getListOfCommands() {
        return new ArrayList<>(commands.values());
    }

    public void dispatch(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        String text = update.message().text().split(" ")[0];
        Command command = commands.getOrDefault(text, commands.get("/unknown"));
        command.handle(update);
    }
}
