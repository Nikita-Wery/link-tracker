package backend.academy.linktracker.scrapper.exception.botexception;

public class BotApiException extends RuntimeException {
    public BotApiException(String message) {
        super(message);
    }
}
