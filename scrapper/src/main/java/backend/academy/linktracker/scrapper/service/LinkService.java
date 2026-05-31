package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
    private final ScrapperMetricsService scrapperMetricsService;

    public LinkService(
            LinkRepository linkRepository,
            ChatLinkRepository chatLinkRepository,
            ScrapperMetricsService scrapperMetricsService) {
        this.linkRepository = linkRepository;
        this.chatLinkRepository = chatLinkRepository;
        this.scrapperMetricsService = scrapperMetricsService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public Link addLink(Link link) {

        try {

            return scrapperMetricsService.timeExternalCall(
                    "database", "addLink", "linkservice", () -> linkRepository.saveAndFlush(link));

        } catch (DataIntegrityViolationException ex) {
            log.warn("Link already exists", kv("link_url", link.getUrl()));
            throw new LinkAlreadyExistsException("Link already exists in repository");
        }
    }

    @Transactional(readOnly = true)
    public Slice<Link> findLinkBatch(long lastLinkId, int size) {

        Slice<Link> slice = scrapperMetricsService.timeExternalCall(
                "database",
                "findByLinkIdGreaterThan",
                "linkservice",
                () -> linkRepository.findByLinkIdGreaterThan(lastLinkId, size));

        List<ChatLink> chatsThatTrackLinks = scrapperMetricsService.timeExternalCall(
                "database",
                "findChatLinksThatTrackLink",
                "linkservice",
                () -> chatLinkRepository.findChatLinksThatTrackLink(slice.getContent().stream()
                        .map(chatlink -> chatlink.getLinkId())
                        .toList()));

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
    public void updateLastUpdateBatch(List<LinkUpdate> batch, int batchSize) {
        Timer.Sample sample = scrapperMetricsService.startRequestTimer();
        linkRepository.updateLastUpdateBatch(batch, batchSize);
        scrapperMetricsService.stopRequestTimer(sample, "database", "updateLastUpdateBatch");
    }
}
