package backend.academy.linktracker.scrapper.dto.bot;

import backend.academy.linktracker.scrapper.utils.validators.annotations.ValidLink;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AddLinkRequest(@NotBlank @ValidLink String link, List<String> tags) {}
