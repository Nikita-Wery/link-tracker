package backend.academy.linktracker.scrapper.domain;

import backend.academy.linktracker.scrapper.config.ResourceType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

@Setter
@Getter
@EqualsAndHashCode
@Builder
public class Link {

    private Long id;

    private URI url;

    private ResourceType resourceType;

    private String description;

    private OffsetDateTime latestUpdateTime;

    private Set<Long> tgChatIds;

    private Set<String> tags;

    private Set<String> filters;

}
