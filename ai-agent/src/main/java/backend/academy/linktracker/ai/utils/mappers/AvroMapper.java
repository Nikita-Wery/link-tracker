package backend.academy.linktracker.ai.utils.mappers;

import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.model.RawLinkUpdate;
import backend.academy.linktracker.contract.avro.ProcessedLinkUpdateEvent;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import org.springframework.stereotype.Component;

@Component
public class AvroMapper {

    public RawLinkUpdate rawLinkUpdateEventToRawLinkUpdate(RawLinkUpdateEvent rawLinkUpdate) {
        return new RawLinkUpdate(
                rawLinkUpdate.getId(),
                rawLinkUpdate.getAuthor().toString(),
                rawLinkUpdate.getUrl().toString(),
                rawLinkUpdate.getDescription().toString(),
                rawLinkUpdate.getTgChatIds());
    }

    public ProcessedLinkUpdateEvent processedLinkUpdateToEvent(ProcessedLinkUpdate processedLinkUpdate) {
        return new ProcessedLinkUpdateEvent(
                processedLinkUpdate.id(),
                processedLinkUpdate.url(),
                processedLinkUpdate.description(),
                processedLinkUpdate.tgChatIds(),
                processedLinkUpdate.priority());
    }
}
