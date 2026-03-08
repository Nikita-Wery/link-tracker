package backend.academy.linktracker.scrapper.exception;

public class NonRetryableHttpException extends RuntimeException {

    private final int status;

    public NonRetryableHttpException(int status, String message) {
        super(message);
        this.status = status;
    }
}
