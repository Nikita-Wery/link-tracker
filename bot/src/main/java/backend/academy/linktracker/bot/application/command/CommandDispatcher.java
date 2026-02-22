package backend.academy.linktracker.bot.application.command;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandDispatcher {

    private final Logger log = LogManager.getLogger(CommandDispatcher.class);

    private Map<String,Command> commands = new HashMap<>();

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
        if (update.message() == null || update.message().text() == null)  {
            return;
        }
        String text = update.message().text().split(" ")[0];
        Command command = commands.getOrDefault(text, commands.get("/unknown"));
        command.handle(update);
    }


}
