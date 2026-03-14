package backend.academy.linktracker.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record StackOverflowQuestionUpdateTime(
        @JsonProperty("question_id") Integer questionId,

        @JsonProperty("last_activity_date") OffsetDateTime lastUpdate,

        @JsonProperty("last_edit_date") OffsetDateTime lastEdit) {}
