package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class LinkAlreadyTrackedException extends RuntimeException {
    public LinkAlreadyTrackedException(String message) {
        super(message);
    }
}
