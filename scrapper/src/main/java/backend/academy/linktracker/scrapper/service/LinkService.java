package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;

    public LinkService(LinkRepository linkRepository, ChatRepository chatRepository) {
        this.linkRepository = linkRepository;
        this.chatRepository = chatRepository;
    }

    public void changeLastUpdate(Link link, OffsetDateTime newLastUpdate) {
        linkRepository.updateLastUpdate(link, newLastUpdate);
    }

    public Optional<Link> getLinkByURI(URI url) {
        return linkRepository.findLinkByURI(url);
    }

}
