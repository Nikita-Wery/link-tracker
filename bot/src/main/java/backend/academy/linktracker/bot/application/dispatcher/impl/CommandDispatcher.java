package backend.academy.linktracker.bot.application.dispatcher.impl;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.dispatcher.UpdateDispatcher;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Обработка события - команда
 *
 * @author Luzin Nikita
 */
@Component
public class CommandDispatcher implements UpdateDispatcher {

    private final Logger log = LogManager.getLogger(CommandDispatcher.class);

    private Map<String, Command<Update>> commands = new HashMap<>();

    private Command<Update> unknownCommand;

    public CommandDispatcher(List<Command<Update>> commands) {
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

    public List<Command<Update>> getListOfCommands() {
        return new ArrayList<>(commands.values());
    }

    /**
     * Обработать команду от пользователя
     *
     * @param update содержит необходимую информацию для обработки
     */
    public void dispatch(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return;
        }

        String text = update.message().text().split(" ")[0];

        Command<Update> command = commands.getOrDefault(text, unknownCommand);
        command.handle(update);
    }

    /**
     * {@inheritDoc}
     *
     * @param update информация о событии
     * @return поддержка обработки
     */
    @Override
    public boolean supports(Update update) {
        if (update.message() != null
                || update.message().text() != null && update.message().text().startsWith("/")) {

            return true;
        } else return false;
    }
}
