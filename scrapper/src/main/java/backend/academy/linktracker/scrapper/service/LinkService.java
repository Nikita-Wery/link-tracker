package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository, ChatRepository chatRepository) {
        this.linkRepository = linkRepository;
    }

    public void changeLastUpdate(Link link, OffsetDateTime newLastUpdate) {
        linkRepository.updateLastUpdate(link, newLastUpdate);
    }
}
