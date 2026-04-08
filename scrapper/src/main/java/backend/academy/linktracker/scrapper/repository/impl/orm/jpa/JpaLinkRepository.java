package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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

    List<Link> findLinkByLinkIdGreaterThan(Long linkIdIsGreaterThan, Pageable pageable);
}
