package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.AbstractCommand;
import com.pengrad.telegrambot.model.Update;

public class ListCommand extends AbstractCommand<Update> {

    public ListCommand() {
        super("/list", "Вывести список всех ссылок, отслеживаемых пользователем");
    }

    @Override
    public void handle(Update data) {
    }
}
