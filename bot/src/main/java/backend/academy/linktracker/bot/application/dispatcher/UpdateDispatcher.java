package backend.academy.linktracker.bot.application.dispatcher;

import com.pengrad.telegrambot.model.Update;

/**
 * Абстракция для обработки разных типов
 * событий
 *
 * @author Luzin Nikita
 */
public interface UpdateDispatcher {

    /**
     * Проверяет, может ли обработчик
     * приступить к обработке события
     *
     * @param update - событие
     * @return поддержка обработки
     */
    boolean supports(Update update);

    /**
     * Обработка события
     *
     * @param update информация, необходимая для обработки
     */
    void dispatch(Update update);
}
