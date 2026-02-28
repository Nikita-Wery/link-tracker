package backend.academy.linktracker.bot.application;

import backend.academy.linktracker.bot.application.dispatcher.UpdateDispatcher;
import backend.academy.linktracker.bot.application.dispatcher.impl.DefaultDispatcher;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Распределяет события по обработчикам
 * {@link UpdateDispatcher}
 *
 * @author Luzin Nikita
 */
@Component
public class UpdateRouter {

    private final List<UpdateDispatcher> dispatchers;
    private final DefaultDispatcher defaultDispatcher;

    public UpdateRouter(List<UpdateDispatcher> dispatchers, DefaultDispatcher defaultDispatcher) {
        this.dispatchers = dispatchers;
        this.defaultDispatcher = defaultDispatcher;
    }

    public void route(Update update) {
        dispatchers.stream()
                .filter(d -> d.supports(update))
                .findFirst()
                .ifPresentOrElse(d -> d.dispatch(update), () -> defaultDispatcher.dispatch(update));
    }
}
