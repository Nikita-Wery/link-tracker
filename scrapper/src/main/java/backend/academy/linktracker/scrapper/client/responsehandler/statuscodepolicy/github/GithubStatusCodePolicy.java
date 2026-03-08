package backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy.github;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy.RetryPolicy;

public class GithubStatusCodePolicy implements RetryPolicy {
    @Override
    public boolean shouldRetry(int statusCode) {
        return statusCode == 429 || statusCode >= 500;
    }
}
