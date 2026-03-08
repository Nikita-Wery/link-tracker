package backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.stackoverflow;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.StatusCodeDefaultHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class StackOverflowStatusCodeDefaultHandler implements StatusCodeDefaultHandler {

    private final Set<Integer> supportedCodes;

    protected StackOverflowStatusCodeDefaultHandler(Set<Integer> supportedCodes) {
        this.supportedCodes = Collections.unmodifiableSet(new HashSet<>(supportedCodes));
    }

    @Override
    public boolean supportsCode(int code) {
        return supportedCodes.contains(code);
    }

}
