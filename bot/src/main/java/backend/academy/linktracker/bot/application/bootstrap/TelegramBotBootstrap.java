package backend.academy.linktracker.bot.application.bootstrap;

import backend.academy.linktracker.bot.application.listener.TelegramPollingUpdateListener;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotBootstrap implements ApplicationRunner {

    private final TelegramPollingUpdateListener listener;
    private final TelegramCommandRegistrar commandRegistrar;

    public TelegramBotBootstrap(
            TelegramPollingUpdateListener listener,
            TelegramCommandRegistrar commandRegistrar
    ) {
        this.listener = listener;
        this.commandRegistrar = commandRegistrar;
    }

    @Override
    public void run(ApplicationArguments args) {
        commandRegistrar.registerCommands();
        listener.start();
    }
}
