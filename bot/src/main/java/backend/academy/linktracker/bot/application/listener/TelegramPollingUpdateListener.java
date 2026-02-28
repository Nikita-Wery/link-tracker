package backend.academy.linktracker.bot.application.listener;

import backend.academy.linktracker.bot.application.UpdateRouter;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Реализация слушателя событий telegramAPI
 * через LongPolling
 *
 * !НЕ ПОДДЕРЖИВАЕТ WebHook
 *
 * @author Luzin Nikita
 */
@Slf4j
@Component
public class TelegramPollingUpdateListener {

    private final TelegramBot telegramBot;
    private final UpdateRouter updateRouter;

    public TelegramPollingUpdateListener(TelegramBot telegramBot, UpdateRouter updateRouter) {
        this.telegramBot = telegramBot;
        this.updateRouter = updateRouter;
    }

    public void start() {
        telegramBot.setUpdatesListener(
                updates -> {
                    updates.forEach(updateRouter::route);

                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> {
                    log.error(e.getMessage());
                });
    }
}
