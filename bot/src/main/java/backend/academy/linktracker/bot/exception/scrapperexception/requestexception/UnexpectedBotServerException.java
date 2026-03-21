package backend.academy.linktracker.bot.exception.scrapperexception.requestexception;

public class UnexpectedBotServerException extends RuntimeException {

    public static final String INTERNAL_BOT_SERVER_EXCEPTION = "Неизвестная внутренняя ошибка";

    public UnexpectedBotServerException(String message) {
        super(message);
    }

    public String getInternalBotServerException() {
        return INTERNAL_BOT_SERVER_EXCEPTION;
    }
}
