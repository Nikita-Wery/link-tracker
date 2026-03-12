package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class LinkNotTrackedException extends RuntimeException {
    public LinkNotTrackedException(String message) {
        super(message);
    }
}
