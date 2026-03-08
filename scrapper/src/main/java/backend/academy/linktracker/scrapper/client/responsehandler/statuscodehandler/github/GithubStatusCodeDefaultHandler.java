package backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.github;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.StatusCodeDefaultHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class GithubStatusCodeDefaultHandler implements StatusCodeDefaultHandler {

    private final Set<Integer> supportedCodes;

    protected GithubStatusCodeDefaultHandler(Set<Integer> supportedCodes) {
        this.supportedCodes = Collections.unmodifiableSet(new HashSet<>(supportedCodes));
    }

    @Override
    public boolean supportsCode(int code) {
        return supportedCodes.contains(code);
    }

}
