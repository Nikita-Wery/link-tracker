package backend.academy.linktracker.scrapper.dto.bot;

import java.util.List;

public record ListLinkResponse(
    List<LinkResponse> links,
    Integer size
) {
}
