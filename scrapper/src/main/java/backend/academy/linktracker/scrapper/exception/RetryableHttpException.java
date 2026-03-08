package backend.academy.linktracker.scrapper.exception;

public class RetryableHttpException extends RuntimeException {

    private final int status;

    public RetryableHttpException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
