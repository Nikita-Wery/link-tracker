package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.time.OffsetDateTime;
import java.util.List;

public interface LinkRepository {

    List<Link> findAll();

    void updateLastUpdate(Link link, OffsetDateTime offsetDateTime);
}
