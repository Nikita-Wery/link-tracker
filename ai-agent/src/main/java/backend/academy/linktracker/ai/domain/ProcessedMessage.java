package backend.academy.linktracker.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;
import jakarta.persistence.Id;
import java.time.OffsetDateTime;

@Entity
@Table(name = "processed_messages")
@NoArgsConstructor
public class ProcessedMessage {

    @Id
    @Column(name = "message_id")
    private Long id;

    @CurrentTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime created_at;

    public ProcessedMessage(Long id) {
        this.id = id;
    }
}
