package backend.academy.linktracker.scrapper.parser.impl;

import backend.academy.linktracker.scrapper.dto.github.RepoInfo;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
public class GithubLinkParser implements LinkParser<RepoInfo> {

    @Override
    public boolean supports(String url) {
        try {
            URI uri = URI.create(url);

            if (!"github.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String[] segments = uri.getPath().split("/");

            return segments.length >= 3
                    && !segments[1].isBlank()
                    && !segments[2].isBlank();

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public RepoInfo parse(URI url) {

        String[] segments = url.getPath().split("/");

        String owner = segments[1];
        String repo = segments[2];

        if (repo.endsWith(".git")) {
            repo = repo.substring(0, repo.length() - 4);
        }

        return new RepoInfo(owner, repo);
    }
}
