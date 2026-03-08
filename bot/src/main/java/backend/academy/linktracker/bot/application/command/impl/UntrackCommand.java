package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.AbstractCommand;
import com.pengrad.telegrambot.model.Update;

public class UntrackCommand extends AbstractCommand<Update> {

    public UntrackCommand() {
        super("/untrack", "Прекратить отслеживаение ссылки");
    }

    @Override
    public void handle(Update data) {

    }
}
