package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class InMemeoryLinkRepository implements LinkRepository {

    private List<Link> linkRepository;

    @Override
    public List<Link> findAll() {
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


}
