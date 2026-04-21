package backend.academy.linktracker.scrapper.dto;

import backend.academy.linktracker.scrapper.config.ResourceType;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

public record LinkUpdate(
        Long id, URI url, String description, Set<Long> tgChatIds, ResourceType resourceType, OffsetDateTime lastUpdate)
        implements UpdateEvent {

    @Override
    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public ResourceType getResourceType() {
        return resourceType;
    }
}
