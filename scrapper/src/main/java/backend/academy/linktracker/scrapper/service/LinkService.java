package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LinkService {

    private final LinkRepository linkRepository;
    private final ChatLinkRepository chatLinkRepository;

    public LinkService(LinkRepository linkRepository, ChatLinkRepository chatLinkRepository) {
        this.linkRepository = linkRepository;
        this.chatLinkRepository = chatLinkRepository;
    }

    @Transactional
    public void changeLastUpdate(Link link, OffsetDateTime newLastUpdate) {
        linkRepository.updateLastUpdate(link, newLastUpdate);
    }

    @Transactional(readOnly = true)
    public Optional<Link> findLinkByUri(String url) {
        return linkRepository.findLinkByURI(url);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public Link addLink(Link link) {

        try {

            return linkRepository.saveAndFlush(link);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Link already exists", kv("link_url", link.getUrl()));
            throw new LinkAlreadyExistsException("Link already exists in repository");
        }
    }

    @Transactional(readOnly = true)
    public Slice<Link> findLinkBatch(long lastLinkId, int size) {

        Slice<Link> slice = linkRepository.findByLinkIdGreaterThan(lastLinkId, size);

        List<ChatLink> chatsThatTrackLinks = chatLinkRepository.findChatLinksThatTrackLink(slice.getContent().stream()
                .map(chatlink -> chatlink.getLinkId())
                .toList());

        Map<Long, Link> futureLinkUpdates = slice.getContent().stream()
                .collect(Collectors.toMap(l -> l.getLinkId(), l -> {
                    Link futureUpdatedLink = new Link(l.getUrl(), l.getResourceType(), l.getLatestUpdateTime());
                    futureUpdatedLink.setLinkId(l.getLinkId());

                    return futureUpdatedLink;
                }));

        for (ChatLink chatLink : chatsThatTrackLinks) {
            futureLinkUpdates.get(chatLink.getLinkId()).getTrackingChats().add(chatLink);
        }

        List<Link> result = slice.getContent().stream()
                .map(l -> futureLinkUpdates.get(l.getLinkId()))
                .toList();

        return new SliceImpl<>(result, slice.getPageable(), slice.hasNext());
    }

    @Transactional
    public void updateLastUpdateBatch(List<UpdateLinkDto> batch, int batchSize) {
        try {
            linkRepository.updateLastUpdateBatch(batch, batchSize);
        } catch (DataAccessException e) {
            log.error("Failed to update last update batch", e);
        }
    }
}
