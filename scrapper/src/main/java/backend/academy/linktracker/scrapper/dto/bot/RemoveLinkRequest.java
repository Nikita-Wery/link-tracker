package backend.academy.linktracker.scrapper.dto.bot;

import backend.academy.linktracker.scrapper.utils.validators.annotations.ValidLink;
import jakarta.validation.constraints.NotBlank;

public record RemoveLinkRequest(@NotBlank @ValidLink String link) {}
