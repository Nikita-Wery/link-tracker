package backend.academy.linktracker.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubIssueResponse (

        String title,

        String body,

        @JsonProperty("html_url")
        String url,

        User user,

        @JsonProperty("created_at")
        OffsetDateTime createdAt,

        @JsonProperty("updated_at")
        OffsetDateTime updatedAt,

        @JsonProperty("pull_request")
        Object pullRequest
) {

    public record User(
            String login
    ) {}
}
