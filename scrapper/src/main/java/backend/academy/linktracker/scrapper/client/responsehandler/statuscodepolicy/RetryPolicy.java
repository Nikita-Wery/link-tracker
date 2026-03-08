package backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy;

public interface RetryPolicy {

    boolean shouldRetry(int statusCode);

}
