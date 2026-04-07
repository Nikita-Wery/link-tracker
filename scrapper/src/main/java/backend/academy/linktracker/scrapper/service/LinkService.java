package backend.academy.linktracker.scrapper.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    public void changeLastUpdate(Link link, OffsetDateTime newLastUpdate) {
        linkRepository.updateLastUpdate(link, newLastUpdate);
    }

    @Transactional
    public Optional<Link> findLinkByUri(String url) {
        return linkRepository.findLinkByURI(url);
    }

    @Transactional
    public Set<Link> findAllLinks() {
        return linkRepository.findAll();
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
}
