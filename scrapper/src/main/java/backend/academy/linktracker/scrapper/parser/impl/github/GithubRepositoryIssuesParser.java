package backend.academy.linktracker.scrapper.parser.impl.github;

import backend.academy.linktracker.scrapper.dto.github.RepoInfo;
import backend.academy.linktracker.scrapper.parser.LinkParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.net.URI;

@Component
@AllArgsConstructor
public class GithubRepositoryIssuesParser implements LinkParser<RepoInfo> {

    private GithubRepositoryLinkParser githubRepositoryLinkParser;

    @Override
    public boolean supports(String url) {
        try {
            URI uri = URI.create(url);

            if (!"github.com".equalsIgnoreCase(uri.getHost())) {
                return false;
            }

            String[] segments = uri.getPath().split("/");

            return segments.length >= 3 && !segments[1].isBlank() && !segments[2].isBlank() && !segments[3].isBlank() && segments[3].equals("issues");

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public RepoInfo parse(URI url) {
        return githubRepositoryLinkParser.parse(url);
    }
}
