package backend.academy.linktracker.bot.dto;

import java.util.List;
import java.util.Set;

public record AddLinkRequest(
    String link,
    Set<String> tags,
    Set<String> filters
) {
}
