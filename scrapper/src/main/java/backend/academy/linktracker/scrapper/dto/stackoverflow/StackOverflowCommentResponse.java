package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowCommentResponse(
        @JsonProperty("comment_id") long id,

        @JsonProperty("owner") StackOverflowUserResponse user,

        @JsonProperty("creation_date") long createdAt,

        String body) {}
