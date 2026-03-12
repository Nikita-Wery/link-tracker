package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class LinkNotExistsException extends RuntimeException {
    public LinkNotExistsException(String message) {
        super(message);
    }
}
