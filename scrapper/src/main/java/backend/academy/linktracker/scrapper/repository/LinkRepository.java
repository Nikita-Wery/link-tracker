package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

public interface LinkRepository {

    Set<Link> findBatchLink(long lastLinkId, int size);

    void updateLastUpdate(Link link, OffsetDateTime newLatestUpdateTime);

    Link save(Link link);

    Link saveAndFlush(Link link);

    Optional<Link> findLinkByURI(String uri);

    boolean existsById(long linkId);

    Set<Link> findAll();
}
