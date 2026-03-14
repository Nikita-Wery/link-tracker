package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.AbstractCommand;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class CancelCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/cancel";
    public static final String COMMAND_DESCRIPTION = "Прервать выполнение текущего диалога";

    public CancelCommand() {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
    }

    @Override
    public void handle(Update data) {}
}
