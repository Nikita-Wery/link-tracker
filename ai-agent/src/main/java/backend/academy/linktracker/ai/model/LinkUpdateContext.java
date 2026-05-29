package backend.academy.linktracker.ai.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class LinkUpdateContext {

    private RawLinkUpdate rawLinkUpdate;

    private String summarizedDescription;

    private Priority priority;
}
