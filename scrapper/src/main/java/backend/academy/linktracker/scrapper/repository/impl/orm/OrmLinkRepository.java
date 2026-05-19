package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcHelper;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaLinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OrmLinkRepository implements LinkRepository {

    private final JpaLinkRepository jpaLinkRepository;
    private final JdbcHelper jdbcHelper;

    @Override
    public Slice<Link> findByLinkIdGreaterThan(long lastLinkId, int size) {
        Pageable page = PageRequest.of(0, size, Sort.by("linkId").ascending());
        return jpaLinkRepository.findLinkByLinkIdGreaterThan(lastLinkId, page);
    }

    @Override
    public void updateLastUpdate(Link link, OffsetDateTime newLatestUpdateTime) {
        jpaLinkRepository.updateLastUpdate(link.getLinkId(), newLatestUpdateTime);
    }

    @Override
    public void updateLastUpdateBatch(List<UpdateLinkDto> batch, int batchSize) {
        jdbcHelper.updateLastUpdateBatch(batch, batchSize);
    }

    @Override
    public Link save(Link link) {
        long linkId = jpaLinkRepository.upsertAndGetId(
                link.getUrl(), link.getResourceType().name(), link.getLatestUpdateTime());
        link.setLinkId(linkId);
        return link;
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
}
