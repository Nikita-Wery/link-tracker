package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

@Repository
@AllArgsConstructor
public class JdbcLinkRepository implements LinkRepository {

    // language=sql
    private static final String SELECT_LINK_BY_ID
        = "SELECT link_id, url, resource_type, latest_update_time FROM links WHERE link_id = :linkId";

    // language=sql
    private static final String INSERT_LINK
        = "INSERT INTO links (link_id, url, resource_type, latest_update_time) VALUES (?, ?, ?, ?)";

    // language=sql
    private static final String UPDATE_UPD_TIME
        = "UPDATE links SET latest_update_time = :newLatestUpdTime WHERE link_id = :linkId";

    // language=sql
    private static final String BATCH_SELECT
        = "SELECT link_id, url, resource_type, latest_update_time FROM links WHERE link_id > :lastId ORDER BY id LIMIT :size";

    // language=sql
    private static final String SELECT_BY_URL
        = "SELECT link_id, url, resource_type, latest_update_time FROM links WHERE url = :linkUrl";

    // language=sql
    private static final String SELECT_ALL_LINKS
        = "SELECT link_id, url, resource_type, latest_update_time FROM links";

    // language=sql
    private static final String SELECT_NEXT_ID
        = "SELECT nextval('LINK_SEQUENCE')";

    // language=sql
    private static final String EXISTS_BY_ID
        = "SELECT EXISTS (SELECT 1 FROM links WHERE link_id = :linkId)";

    private static final RowMapper<Link> rsLinkMapper = (rs, rowNum) -> {
        Link link = new Link(
            rs.getString("url"),
            ResourceType.valueOf(rs.getString("resource_type")),
            rs.getObject("latest_update_time", OffsetDateTime.class)
        );

        link.setLinkId(rs.getLong("id"));
        return link;
    };

    private final JdbcClient jdbcClient;

    @Override
    public Set<Link> findBatchLink(long lastLinkId, int size) {
        return jdbcClient.sql(BATCH_SELECT)
            .param("lastId", lastLinkId)
            .param("size", size)
            .query(rsLinkMapper).set();
    }

    @Override
    public void updateLastUpdate(Link link, OffsetDateTime offsetDateTime) {
        jdbcClient.sql(UPDATE_UPD_TIME)
            .param("newLatestUpdTime", offsetDateTime)
            .param("linkId", link.getLinkId())
            .update();
    }

    @Override
    public Link save(Link link) {
        Long id = jdbcClient.sql(SELECT_NEXT_ID)
            .query(Long.class)
            .single();

        link.setLinkId(id);

        jdbcClient.sql(INSERT_LINK)
            .param(1, link.getLinkId())
            .param(2, link.getUrl())
            .param(3, link.getResourceType().name())
            .param(4, link.getLatestUpdateTime())
            .update();

        return link;
    }

    @Override
    public Optional<Link> findLinkByURI(String uri) {
        return jdbcClient.sql(SELECT_BY_URL)
            .param(uri.toString())
            .query(rsLinkMapper)
            .optional();
    }

    @Override
    public boolean existsById(long linkId) {
        return jdbcClient.sql(EXISTS_BY_ID)
            .param("linkId", linkId)
            .query(Boolean.class)
            .single();
    }

    @Override
    public Set<Link> findAll() {
        return jdbcClient.sql(SELECT_ALL_LINKS)
            .query(rsLinkMapper)
            .set();
    }
}
