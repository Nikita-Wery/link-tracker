package backend.academy.linktracker.scrapper.source;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import java.util.Optional;

public interface UpdateSource {

    ResourceType getResourceType();

    UpdateEvent getUpdates(Link link);

}
