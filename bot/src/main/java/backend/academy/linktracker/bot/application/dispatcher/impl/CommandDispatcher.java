package backend.academy.linktracker.bot.application.dispatcher.impl;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.dispatcher.UpdateDispatcher;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.CommandValidator;
import com.pengrad.telegrambot.model.Update;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Обработка события - команда
 *
 * @author Luzin Nikita
 */
@Slf4j
@Component
public class CommandDispatcher implements UpdateDispatcher {

    private final Map<String, Command<Update>> commands = new ConcurrentHashMap<>();
    private final CommandValidator commandValidator;
    private final DialogContextStorage contextStorage;
    private Command<Update> unknownCommand;

    public CommandDispatcher(
            List<Command<Update>> commands, CommandValidator commandValidator, DialogContextStorage contextStorage) {

        commands.forEach(command -> {
            if (!command.getCommandName().equals("/unknown")) {
                this.commands.put(command.getCommandName(), command);
            } else {
                unknownCommand = command;
            }
        });
        this.commandValidator = commandValidator;
        this.contextStorage = contextStorage;
    }

    @PostConstruct
    public void logCommands() {
        log.info("Commands loaded in Dispatcher:");
        commands.keySet().forEach(cmd -> log.info("Command loaded: {}", cmd));
    }

    public List<Command<Update>> getListOfCommands() {
        return new ArrayList<>(commands.values());
    }

    /**
     * Обработать команду от пользователя
     *
     * @param update содержит необходимую информацию для обработки
     */
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void dispatch(Update update) {
        if (update.message() == null || update.message().text() == null) {
            log.error("The update did not have a message", kv("telegram_update", update));
        } else {
            String text = update.message().text().split(" ")[0].trim();

            Command<Update> command = commands.getOrDefault(text, unknownCommand);
            contextStorage.clearDialog(update.message().chat().id());
            command.handle(update);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param update информация о событии
     * @return поддержка обработки
     */
    @Override
    public boolean supports(Update update) {
        return update.message() != null
                && !update.message().text().isBlank()
                && commandValidator.isCommand(
                        update.message().text().split(" ")[0].trim());
    }
}
