package backend.academy.linktracker.scrapper.repository.impl;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {

    private final Set<Link> linkRepository;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public InMemoryLinkRepository() {
        this.linkRepository = new HashSet<>();
    }

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
        link.getId();

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

    @Override
    public boolean deleteTrackingChat(Link link, ChatLink chatLink) {
        boolean chatUnpinned = false;
        Iterator<ChatLink> iterator = link.getTrackingChats().iterator();

        while (iterator.hasNext()) {
            ChatLink chatLinkNow = iterator.next();

            if (chatLinkNow.equals(chatLink)) {
                iterator.remove();
                chatUnpinned = true;
            }
        }

        return chatUnpinned;
    }
}
