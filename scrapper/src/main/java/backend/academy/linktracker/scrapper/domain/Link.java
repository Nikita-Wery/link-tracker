package backend.academy.linktracker.scrapper.domain;

import backend.academy.linktracker.scrapper.config.ResourceType;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "links")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    @Id
    @Column(name = "link_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "link_seq")
    @SequenceGenerator(
        name = "link_seq",
        sequenceName = "link_sequence"
    )
    private Long linkId;

    // TODO: навесить index
    @NotNull
    @Column(nullable = false, length = 2048, unique = true, updatable = false)
    private String url;

    @NotNull
    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "latest_update_time")
    private OffsetDateTime latestUpdateTime;

    @OneToMany(mappedBy = "link")
    private Set<ChatLink> trackingChats = new HashSet<>();

    public Link(String url, ResourceType resourceType, OffsetDateTime latestUpdateTime) {
        this.url = url;
        this.resourceType = resourceType;
        this.latestUpdateTime = latestUpdateTime;
    }

    public boolean addChat(ChatLink chatLink) {
        return trackingChats.add(chatLink);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && o instanceof Link) {
            Link that = (Link) o;
            return this.url.equals(that.url) && this.resourceType.equals(that.resourceType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode() + resourceType.hashCode();
    }

}
