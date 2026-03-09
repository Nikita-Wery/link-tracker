package backend.academy.linktracker.scrapper.dto.bot;

import java.net.URI;

public record RemoveLinkRequest(
    URI link
) {
}
