package backend.academy.linktracker.bot.application.command;

public abstract class AbstractCommand<T> implements Command<T> {

    private final String name;

    private final String description;

    public AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getCommandName() {
        return name;
    }

    public String getCommandDescription() {
        return description;
    }
}
