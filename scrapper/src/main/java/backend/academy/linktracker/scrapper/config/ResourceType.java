package backend.academy.linktracker.scrapper.config;

import java.util.regex.Pattern;

public enum ResourceType {

    GITHUB_REPOSITORY(
            Pattern.compile("^https://api\\.github\\.com/repos/([^/]+)/([^/]+)$")
    ),

    // TODO: поменять паттерн возможно стоит
    STACKOVERFLOW_QUESTION(
            Pattern.compile("^https://api\\.stackexchange\\.com/2\\.3/questions/(\\d+)$")
    );

    private final Pattern pattern;

    ResourceType(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern pattern() {
        return pattern;
    }
}
