package backend.academy.linktracker.scrapper.dto.bot;

import java.util.List;
import lombok.Builder;

@Builder
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stackTrace) {}
