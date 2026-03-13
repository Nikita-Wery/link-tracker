package backend.academy.linktracker.scrapper.dto.bot;

import java.util.List;

public record ListLinksResponse(
    List<LinkResponse> links,
    Integer size
) {
}
