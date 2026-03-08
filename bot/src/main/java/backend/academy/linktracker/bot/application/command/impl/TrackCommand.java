package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.AbstractCommand;
import com.pengrad.telegrambot.model.Update;

public class TrackCommand extends AbstractCommand<Update> {

    public TrackCommand() {
        super("/track", "Начать отслеживание ссылки");
    }

    @Override
    public void handle(Update data) {

    }
}
