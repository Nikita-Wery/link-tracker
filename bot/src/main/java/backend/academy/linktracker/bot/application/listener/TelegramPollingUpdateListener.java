package backend.academy.linktracker.bot.application.listener;

import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class TelegramPollingUpdateListener {

    private final Logger log = LogManager.getLogger(TelegramPollingUpdateListener.class);

    private final TelegramBot telegramBot;
    private final CommandDispatcher commandDispatcher;

    public TelegramPollingUpdateListener(TelegramBot telegramBot, CommandDispatcher commandDispatcher) {
        this.telegramBot = telegramBot;
        this.commandDispatcher = commandDispatcher;
    }

    @PostConstruct
    public void start() {
        telegramBot.setUpdatesListener(updates -> {

            log.info("Received {} updates", updates.size());

            updates.forEach(commandDispatcher::dispatch);

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
