package backend.academy.linktracker.bot.application.command;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CommandDispatcher {

    private final Logger log = LogManager.getLogger(CommandDispatcher.class);

    private Map<String, Command> commands = new HashMap<>();

    private Command unknownCommand;

    public CommandDispatcher(List<Command> commands) {
        commands.stream().forEach(command -> {
            if (!command.getCommandName().equals("/unknown")) {
                this.commands.put(command.getCommandName(), command);
            } else {
                unknownCommand = command;
            }
        });
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

        String language = Optional.ofNullable(update.message())
                .map(Message::from)
                .map(User::languageCode)
                .orElse("unknown");

        log.info("LANGUAGE: {}", language);

        String text = update.message().text().split(" ")[0];

        Command command = commands.getOrDefault(text, unknownCommand);
        command.handle(update);
    }
}
