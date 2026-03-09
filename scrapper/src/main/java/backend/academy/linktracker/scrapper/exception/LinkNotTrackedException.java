package backend.academy.linktracker.scrapper.exception;

public class LinkNotTrackedException extends RuntimeException {
    public LinkNotTrackedException(String message) {
        super(message);
    }
}
