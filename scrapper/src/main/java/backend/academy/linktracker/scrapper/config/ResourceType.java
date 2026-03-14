package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.parser.impl.GithubLinkParser;
import backend.academy.linktracker.scrapper.parser.impl.StackOverflowLinkParser;

public enum ResourceType {
    GITHUB_REPOSITORY(new GithubLinkParser()),

    STACKOVERFLOW_QUESTION(new StackOverflowLinkParser());

    private final LinkParser<?> parser;

    ResourceType(LinkParser<?> parser) {
        this.parser = parser;
    }

    public LinkParser<?> parser() {
        return parser;
    }
}
