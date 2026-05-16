package backend.academy.linktracker.bot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.annotations.CurrentTimestamp;
import java.time.OffsetDateTime;

@Entity
public class ProcessedMessages {

    @Id
    private Long id;

    @CurrentTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime created_at;

}
