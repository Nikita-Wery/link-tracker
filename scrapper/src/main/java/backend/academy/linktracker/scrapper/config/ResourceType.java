package backend.academy.linktracker.scrapper.config;

import backend.academy.linktracker.scrapper.parser.LinkParser;
import backend.academy.linktracker.scrapper.parser.impl.github.GithubRepositoryIssuesParser;
import backend.academy.linktracker.scrapper.parser.impl.github.GithubRepositoryLinkParser;
import backend.academy.linktracker.scrapper.parser.impl.stackoverflow.StackOverflowAnswersParser;
import backend.academy.linktracker.scrapper.parser.impl.stackoverflow.StackOverflowCommentsParser;
import backend.academy.linktracker.scrapper.parser.impl.stackoverflow.StackOverflowLinkParser;

public enum ResourceType {
    GITHUB_REPOSITORY(new GithubRepositoryLinkParser()),

    GITHUB_REPOSITORY_ISSUE(new GithubRepositoryIssuesParser(new GithubRepositoryLinkParser())),

    STACKOVERFLOW_QUESTION(new StackOverflowLinkParser()),

    STACKOVERFLOW_COMMENTS(new StackOverflowCommentsParser()),

    STACKOVERFLOW_ANSWERS(new StackOverflowAnswersParser());

    private final LinkParser<?> parser;

    ResourceType(LinkParser<?> parser) {
        this.parser = parser;
    }

    public LinkParser<?> parser() {
        return parser;
    }
}
