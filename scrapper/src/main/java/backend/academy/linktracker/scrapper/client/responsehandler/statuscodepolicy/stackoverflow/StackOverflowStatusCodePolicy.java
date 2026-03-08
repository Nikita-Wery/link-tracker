package backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy.stackoverflow;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy.RetryPolicy;

public class StackOverflowStatusCodePolicy implements RetryPolicy {

    @Override
    public boolean shouldRetry(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }
}
