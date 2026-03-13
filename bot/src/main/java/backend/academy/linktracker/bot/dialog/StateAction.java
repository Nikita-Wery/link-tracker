package backend.academy.linktracker.bot.dialog;

import com.pengrad.telegrambot.model.Update;

public interface StateAction {

    boolean execute(Update update, DialogContext context);
}
