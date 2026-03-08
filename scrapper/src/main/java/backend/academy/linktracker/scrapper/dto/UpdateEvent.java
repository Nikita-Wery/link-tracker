package backend.academy.linktracker.scrapper.dto;

import backend.academy.linktracker.scrapper.config.ResourceType;
import java.time.OffsetDateTime;

public sealed interface UpdateEvent permits LinkUpdate {

    OffsetDateTime getLastUpdate();

    ResourceType getResourceType();
}
