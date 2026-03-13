package backend.academy.linktracker.bot.dialoge;

import com.pengrad.telegrambot.model.Update;

public interface StateAction {

    void execute(Update update, DialogContext context);
}
