package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaLinkRepository;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OrmLinkRepository implements LinkRepository {

    private JpaLinkRepository jpaLinkRepository;

    @Override
    public Set<Link> findBatchLink(long lastLinkId, int size) {
        return jpaLinkRepository.findBatchLink(lastLinkId, size);
    }

    @Override
    public void updateLastUpdate(Link link, OffsetDateTime newLatestUpdateTime) {
        jpaLinkRepository.updateLastUpdate(link.getLinkId(), newLatestUpdateTime);
    }

    @Override
    public Link save(Link link) {
        return jpaLinkRepository.save(link);
    }

    @Override
    public Link saveAndFlush(Link link) {
        Link savedLink = save(link);
        jpaLinkRepository.flush();
        return savedLink;
    }

    @Override
    public Optional<Link> findLinkByURI(String uri) {
        return jpaLinkRepository.findLinkByUrl(uri);
    }

    @Override
    public boolean existsById(long linkId) {
        return jpaLinkRepository.existsById(linkId);
    }

    @Override
    public Set<Link> findAll() {
        return new HashSet(jpaLinkRepository.findAll());
    }
}
