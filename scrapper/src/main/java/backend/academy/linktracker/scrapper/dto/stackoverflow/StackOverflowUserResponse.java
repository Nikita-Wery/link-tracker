package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowUserResponse(
    @JsonProperty("display_name")
    String name
) {
}
