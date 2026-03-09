package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import org.springframework.stereotype.Repository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class InMemeoryLinkRepository implements LinkRepository {

    private Set<Link> linkRepository;

    @Override
    public Set<Link> findAll() {
        return linkRepository;
    }

    @Override
    public void updateLastUpdate(Link link, OffsetDateTime latestUpdateTime) {
        for (Link changableLink : linkRepository) {
            if (changableLink.equals(link)) {
                link.setLatestUpdateTime(latestUpdateTime);
            }
        }
    }

    @Override
    public Link save(Link link) {
        linkRepository.add(link);

        return link;
    }

    @Override
    public Optional<Link> findLinkByURI(URI url) {

        Optional<Link> result = Optional.empty();

        for (Link link : linkRepository) {
            if (link.getUrl().equals(url)) result = Optional.of(link);
        }

        return result;
    }


}
