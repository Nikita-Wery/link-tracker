package backend.academy.linktracker.bot.application.command;

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
}
