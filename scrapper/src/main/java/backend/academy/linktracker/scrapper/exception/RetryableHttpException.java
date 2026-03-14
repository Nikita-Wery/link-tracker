package backend.academy.linktracker.scrapper.exception;

public class RetryableHttpException extends RuntimeException {

    public RetryableHttpException(String message) {
        super(message);
    }
}
