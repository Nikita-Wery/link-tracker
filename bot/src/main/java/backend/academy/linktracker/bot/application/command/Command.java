package backend.academy.linktracker.bot.application.command;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Абстракция команды, используется паттерн "Command"
 *
 * @author Luzin Nikita
 *
 * @param <T> информация, требуемая для команды
 */
public interface Command<T> {

    void handle(T data);

    String getCommandDescription();

    String getCommandName();

    @Autowired
    default void registerMySelf(CommandDispatcher commandDispatcher) {
        commandDispatcher.register(getCommandName(), this);
    }
}
