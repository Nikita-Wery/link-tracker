package backend.academy.linktracker.scrapper.exception;

public class ChatAlreadyExistException extends RuntimeException {
    public ChatAlreadyExistException(String message) {
        super(message);
    }
}
