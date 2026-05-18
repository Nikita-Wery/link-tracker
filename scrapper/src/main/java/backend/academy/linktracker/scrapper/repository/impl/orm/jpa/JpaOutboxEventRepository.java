package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.OutboxEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
        WITH selected AS (
            SELECT id
            FROM outbox_event
            WHERE status = 'PENDING'
              AND topic = :topic
            ORDER BY created_at ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
        )
        UPDATE outbox_event oe
        SET status = 'PROCESSING'
        FROM selected s
        WHERE oe.id = s.id
        RETURNING oe.*;
        """, nativeQuery = true)
    List<OutboxEvent> findBatchPendingMessagesAndChangeStatus(
            @Param("batchSize") int batchSize, @Param("topic") String topic);
}
