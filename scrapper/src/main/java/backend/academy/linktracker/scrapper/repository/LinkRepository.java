package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Slice;

public interface LinkRepository {

    Slice<Link> findByLinkIdGreaterThan(long lastLinkId, int size);

    void updateLastUpdate(Link link, OffsetDateTime newLatestUpdateTime);

    void updateLastUpdateBatch(List<UpdateLinkDto> batch, int batchSize);

    Link save(Link link);

    Link saveAndFlush(Link link);

    Optional<Link> findLinkByURI(String uri);
}
