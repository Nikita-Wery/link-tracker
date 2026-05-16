package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.util.List;

public interface LinkUpdateProcessor {
    void processBatch(List<LinkUpdate> linkUpdates);
}
