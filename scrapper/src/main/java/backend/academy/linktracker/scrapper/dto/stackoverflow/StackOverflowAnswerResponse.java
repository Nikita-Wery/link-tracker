package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowAnswerResponse(
        @JsonProperty("answer_id") long id,

        @JsonProperty("owner") StackOverflowUserResponse user,

        @JsonProperty("creation_date") long createdAt,

        @JsonProperty("last_activity_date") long updatedAt,

        @JsonProperty("is_accepted") boolean accepted,

        String body) {}
