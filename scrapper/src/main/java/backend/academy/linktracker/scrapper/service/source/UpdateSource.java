package backend.academy.linktracker.scrapper.service.source;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import java.util.List;

public interface UpdateSource<T extends UpdateEvent> {

    ResourceType getResourceType();

    List<T> getUpdates(Link link);
}
