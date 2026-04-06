package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    public void changeLastUpdate(Link link, OffsetDateTime newLastUpdate) {
        linkRepository.updateLastUpdate(link, newLastUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addLink(Link link) {
        linkRepository.save(link);
    }
}
