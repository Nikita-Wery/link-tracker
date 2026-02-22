package backend.academy.linktracker.bot.application.client;

public interface TelegramMessageSender {
    void sendMessage(Long chatId, String message);
}
