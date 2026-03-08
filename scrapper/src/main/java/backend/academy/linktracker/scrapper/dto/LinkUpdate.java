package backend.academy.linktracker.scrapper.dto;

import backend.academy.linktracker.scrapper.config.ResourceType;
import lombok.Getter;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public record LinkUpdate(
    URI url,
    String description,
    List<Long> tgChatIds,
    ResourceType resourceType,
    OffsetDateTime lastUpdate
) implements UpdateEvent {

    @Override
    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public ResourceType getResourceType() {
        return resourceType;
    }
}
