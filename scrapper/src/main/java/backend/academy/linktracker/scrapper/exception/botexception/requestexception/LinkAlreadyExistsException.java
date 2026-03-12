package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class LinkAlreadyExistsException extends RuntimeException {
    public LinkAlreadyExistsException(String message) {
        super(message);
    }
}
