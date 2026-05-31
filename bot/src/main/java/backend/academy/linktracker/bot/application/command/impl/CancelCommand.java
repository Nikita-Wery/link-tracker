package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class CancelCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/cancel";
    public static final String COMMAND_DESCRIPTION = "Прервать выполнение текущего диалога";

    private static final String COMMAND_MESSAGE = "Текущее действие успешно прервано";

    private final TelegramMessageSender sender;
    private final BotMetricsService botMetrics;

    public CancelCommand(TelegramMessageSender sender, BotMetricsService botMetrics) {
        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.sender = sender;
        this.botMetrics = botMetrics;
    }

    @Override
    public void handle(Update update) {

        sender.sendMessage(update.message().chat().id(), COMMAND_MESSAGE);
        botMetrics.incrementCommand(COMMAND_NAME);
    }
}
