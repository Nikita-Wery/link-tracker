package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

public class UnexpectedScrapperServerException extends RuntimeException {
    public UnexpectedScrapperServerException(String message) {
        super(message);
    }
}
