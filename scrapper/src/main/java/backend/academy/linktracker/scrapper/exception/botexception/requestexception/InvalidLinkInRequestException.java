package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class InvalidLinkInRequestException extends RuntimeException {
    public InvalidLinkInRequestException(String message) {
        super(message);
    }
}
