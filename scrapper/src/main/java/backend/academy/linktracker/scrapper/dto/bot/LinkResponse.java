package backend.academy.linktracker.scrapper.dto.bot;

import java.net.URI;
import java.util.List;

public record LinkResponse(Long id, String url, List<String> tags) {}
