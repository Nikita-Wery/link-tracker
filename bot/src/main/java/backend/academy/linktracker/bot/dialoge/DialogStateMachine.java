package backend.academy.linktracker.bot.dialoge;

import com.pengrad.telegrambot.model.Update;
import java.util.List;

public interface DialogStateMachine<T extends DialogState> {

    void handle(Update update, DialogContext context);

    List<T> getAllStates();
}
