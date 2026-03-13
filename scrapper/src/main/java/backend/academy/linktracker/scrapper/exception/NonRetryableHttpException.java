package backend.academy.linktracker.scrapper.exception;

public class NonRetryableHttpException extends RuntimeException {

    public NonRetryableHttpException(String message) {
        super(message);
    }
}
