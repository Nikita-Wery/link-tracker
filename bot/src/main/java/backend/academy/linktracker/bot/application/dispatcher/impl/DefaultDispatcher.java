package backend.academy.linktracker.bot.application.dispatcher.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.impl.UnknownCommand;
import backend.academy.linktracker.bot.application.dispatcher.UpdateDispatcher;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

/**
 * Обработчик неизвестных событий
 *
 * @author Luzin Nikita
 */
@Component
public class DefaultDispatcher implements UpdateDispatcher {

    private final TelegramMessageSender telegramMessageSender;

    private final UnknownCommand unknownCommand;

    public DefaultDispatcher(TelegramMessageSender telegramMessageSender, BotMetricsService botMetrics) {
        this.telegramMessageSender = telegramMessageSender;
        this.unknownCommand = new UnknownCommand(telegramMessageSender, botMetrics);
    }

    /**
     * {@inheritDoc}
     *
     * @param update - событие
     * @return поддержка обработки
     */
    @Override
    public boolean supports(Update update) {
        return false;
    }

    /**
     * Способен обработать ЛЮБОЕ событие
     *
     * @param update информация, необходимая для обработки
     */
    @Override
    public void dispatch(Update update) {
        unknownCommand.handle(update);
    }
}
