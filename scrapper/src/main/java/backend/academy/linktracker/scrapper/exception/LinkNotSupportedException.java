package backend.academy.linktracker.scrapper.exception;

public class LinkNotSupportedException extends RuntimeException {
    public LinkNotSupportedException(String message) {
        super(message);
    }
}
