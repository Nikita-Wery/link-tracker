package backend.academy.linktracker.scrapper.exception.responsexception;

public class RetryableHttpException extends RuntimeException {

    public RetryableHttpException(String message) {
        super(message);
    }

}
