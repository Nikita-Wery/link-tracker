package backend.academy.linktracker.bot.application.bootstrap;

import backend.academy.linktracker.bot.application.listener.TelegramPollingUpdateListener;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Стартер приложения
 *
 * @author Luzin Nikita
 */
@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramBotBootstrap implements ApplicationRunner {

    private final TelegramPollingUpdateListener listener;
    private final TelegramCommandRegistrar commandRegistrar;

    public TelegramBotBootstrap(TelegramPollingUpdateListener listener, TelegramCommandRegistrar commandRegistrar) {
        this.listener = listener;
        this.commandRegistrar = commandRegistrar;
    }

    @Override
    public void run(ApplicationArguments args) {
        commandRegistrar.registerCommands();
        listener.start();
    }
}
