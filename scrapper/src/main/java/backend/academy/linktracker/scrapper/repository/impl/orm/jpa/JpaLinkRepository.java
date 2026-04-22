package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.Link;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findLinkByUrl(String url);

    @Modifying
    @Query("""
        UPDATE Link l SET l.latestUpdateTime = :newLatestUpdTime WHERE l.linkId = :linkId
    """)
    void updateLastUpdate(@Param("linkId") long linkId, @Param("newLatestUpdTime") OffsetDateTime newLatestUpdateTime);

    @Query(value = """
        INSERT INTO links (url, resource_type, latest_update_time)
        VALUES (:url, :resourceType, :latestUpdateTime)
        ON CONFLICT (url)
        DO UPDATE SET url = links.url
        RETURNING link_id
        """, nativeQuery = true)
    Long upsertAndGetId(
            @Param("url") String url,
            @Param("resourceType") String resourceType,
            @Param("latestUpdateTime") OffsetDateTime latestUpdateTime);

    Slice<Link> findLinkByLinkIdGreaterThan(Long linkIdIsGreaterThan, Pageable pageable);
}
