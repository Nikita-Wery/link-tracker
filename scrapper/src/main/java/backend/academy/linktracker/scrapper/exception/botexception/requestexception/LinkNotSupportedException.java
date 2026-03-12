package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class LinkNotSupportedException extends RuntimeException {
    public LinkNotSupportedException(String message) {
        super(message);
    }
}
