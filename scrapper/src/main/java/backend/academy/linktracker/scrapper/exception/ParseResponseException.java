package backend.academy.linktracker.scrapper.exception;

public class ParseResponseException extends RuntimeException {
    public ParseResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
