package backend.academy.linktracker.scrapper.domain;

import backend.academy.linktracker.scrapper.config.ResourceType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
public class Link {

    private Long id;

    @NotNull
    private final URI url;

    @NotNull
    private final ResourceType resourceType;

    private OffsetDateTime latestUpdateTime;

    @Builder.Default
    private Set<ChatLink> trackingChats = new HashSet<>();

    public Link(URI url, ResourceType resourceType) {
        this.url = url;
        this.resourceType = resourceType;
    }

    public boolean addChat(ChatLink chatLink) {
        return trackingChats.add(chatLink);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && o instanceof Link) {
            Link that = (Link) o;
            return this.url.equals(that.url)
                && this.resourceType.equals(that.resourceType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode() + resourceType.hashCode();
    }

}
