package backend.academy.linktracker.scrapper.dto.bot;


import jakarta.validation.constraints.NotBlank;

public record RemoveLinkRequest(
    @NotBlank
    String link
) {
}
