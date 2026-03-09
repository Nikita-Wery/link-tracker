package backend.academy.linktracker.scrapper.exception;

public class ChatNotExistException extends RuntimeException {
    public ChatNotExistException(String message) {
        super(message);
    }
}
