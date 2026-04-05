package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findLinkByUrl(String url);

    @Modifying
    @Query("""
        UPDATE Link l SET l.latestUpdateTime = :newLatestUpdTime WHERE l.linkId = :linkId
    """)
    void updateLastUpdate(@Param("linkId") long linkId, @Param("newLatestUpdTime") OffsetDateTime newLatestUpdateTime);

    @Query(value = """
        SELECT * FROM links
        WHERE id > :lastId
        ORDER BY id
        LIMIT :size
        """,
        nativeQuery = true)
    Set<Link> findBatchLink(@Param("lastId") long lastLinkId, @Param("size") int size);

}
