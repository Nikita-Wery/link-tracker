package backend.academy.linktracker.bot.application.client;

public interface TelegramMessageSender {

    /**
     * Послать сообщение в чат
     *
     * Соответствует telegramApi, поэтому
     * тип параметров определён явно
     *
     * @param chatId id чата, в который нужно отправить сообщение
     * @param message отправляемое сообщение
     */
    void sendMessage(Long chatId, String message);
}
