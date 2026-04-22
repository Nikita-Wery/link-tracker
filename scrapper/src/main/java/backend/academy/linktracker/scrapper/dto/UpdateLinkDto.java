package backend.academy.linktracker.scrapper.dto;

import java.time.OffsetDateTime;

public record UpdateLinkDto(long linkId, OffsetDateTime updatedAt) {}
