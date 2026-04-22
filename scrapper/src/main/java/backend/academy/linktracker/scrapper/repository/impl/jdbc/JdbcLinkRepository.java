package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    // language=sql
    private static final String SELECT_LINK_BY_ID =
            "SELECT link_id, url, resource_type, latest_update_time FROM links WHERE link_id = :linkId";

    // language=sql
    private static final String INSERT_LINK =
            "INSERT INTO links (link_id, url, resource_type, latest_update_time) VALUES (:linkId, :linkUrl, :linkRT, :latestUpdTime)";

    // language=sql
    private static final String UPDATE_UPD_TIME =
            "UPDATE links SET latest_update_time = :newLatestUpdTime WHERE link_id = :linkId";

    // language=sql
    private static final String BATCH_SELECT =
            "SELECT link_id, url, resource_type, latest_update_time FROM links WHERE link_id > :lastId ORDER BY link_id LIMIT :size";

    // language=sql
    private static final String SELECT_BY_URL =
            "SELECT link_id, url, resource_type, latest_update_time FROM links WHERE url = :linkUrl";

    // language=sql
    private static final String SELECT_ALL_LINKS = "SELECT link_id, url, resource_type, latest_update_time FROM links";

    // language=sql
    private static final String SELECT_NEXT_ID = "SELECT nextval('LINK_SEQUENCE')";

    // language=sql
    private static final String EXISTS_BY_ID = "SELECT EXISTS (SELECT 1 FROM links WHERE link_id = :linkId)";

    private static final RowMapper<Link> rsLinkMapper = (rs, rowNum) -> {
        Link link = new Link(
                rs.getString("url"),
                ResourceType.valueOf(rs.getString("resource_type")),
                rs.getObject("latest_update_time", OffsetDateTime.class));

        link.setLinkId(rs.getLong("link_id"));
        return link;
    };

    private final JdbcClient jdbcClient;
    private final JdbcHelper jdbcHelper;

    @Override
    public Slice<Link> findByLinkIdGreaterThan(long lastLinkId, int size) {

        List<Link> result = jdbcClient
                .sql(BATCH_SELECT)
                .param("lastId", lastLinkId)
                .param("size", size + 1)
                .query(rsLinkMapper)
                .list();

        boolean hasNext = result.size() > size;

        List<Link> content = hasNext ? result.subList(0, size) : result;

        return new SliceImpl<>(content, PageRequest.of(0, size), hasNext);
    }

    @Override
    public void updateLastUpdate(Link link, OffsetDateTime offsetDateTime) {
        jdbcClient
                .sql(UPDATE_UPD_TIME)
                .param("newLatestUpdTime", offsetDateTime)
                .param("linkId", link.getLinkId())
                .update();
    }

    @Override
    public void updateLastUpdateBatch(List<UpdateLinkDto> batch, int batchSize) {
        jdbcHelper.updateLastUpdateBatch(batch, batchSize);
    }

    @Override
    public Link save(Link link) {
        Long id = jdbcClient.sql(SELECT_NEXT_ID).query(Long.class).single();

        link.setLinkId(id);

        jdbcClient
                .sql(INSERT_LINK)
                .param("linkId", link.getLinkId())
                .param("linkUrl", link.getUrl())
                .param("linkRT", link.getResourceType().name())
                .param("latestUpdTime", link.getLatestUpdateTime())
                .update();

        return link;
    }

    @Override
    public Link saveAndFlush(Link link) {
        return save(link);
    }

    @Override
    public Optional<Link> findLinkByURI(String uri) {
        return jdbcClient
                .sql(SELECT_BY_URL)
                .param("linkUrl", uri.toString())
                .query(rsLinkMapper)
                .optional();
    }

    @Override
    public boolean existsById(long linkId) {
        return jdbcClient
                .sql(EXISTS_BY_ID)
                .param("linkId", linkId)
                .query(Boolean.class)
                .single();
    }

    @Override
    public Set<Link> findAll() {
        return jdbcClient.sql(SELECT_ALL_LINKS).query(rsLinkMapper).set();
    }
}
