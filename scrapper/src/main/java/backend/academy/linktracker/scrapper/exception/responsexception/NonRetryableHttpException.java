package backend.academy.linktracker.scrapper.exception.responsexception;

public class NonRetryableHttpException extends RuntimeException {

    public NonRetryableHttpException(String message) {
        super(message);
    }
}
