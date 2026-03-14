package backend.academy.linktracker.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubRepositoryUpdateTime(
        @JsonProperty("name") String repositoryName,

        @JsonProperty("pushed_at") OffsetDateTime pushedAt,

        @JsonProperty("updated_at") OffsetDateTime updateAt) {}
