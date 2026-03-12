package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class ChatNotExistsException extends RuntimeException {
    public ChatNotExistsException(String message) {
        super(message);
    }
}
