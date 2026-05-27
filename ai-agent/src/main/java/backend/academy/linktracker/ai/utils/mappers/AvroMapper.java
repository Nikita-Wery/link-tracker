package backend.academy.linktracker.ai.utils.mappers;

import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import org.springframework.stereotype.Component;

@Component
public class AvroMapper {

    public RawLinkUpdate rawLinkUpdateEventToRawLinkUpdate(RawLinkUpdateEvent rawLinkUpdate) {
        return new
            RawLinkUpdate(
                rawLinkUpdate.getId(),
                rawLinkUpdate.getAuthor().toString(),
                rawLinkUpdate.getUrl().toString(),
                rawLinkUpdate.getDescription().toString(),
                rawLinkUpdate.getTgChatIds()
            );
    }

}
