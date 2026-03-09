package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

public interface LinkRepository {

    Set<Link> findAll();

    void updateLastUpdate(Link link, OffsetDateTime offsetDateTime);

    Link save(Link link);

    Optional<Link> findLinkByURI(URI uri);

}
