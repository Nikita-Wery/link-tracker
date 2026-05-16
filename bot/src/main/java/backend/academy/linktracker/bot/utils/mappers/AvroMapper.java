package backend.academy.linktracker.bot.utils.mappers;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import org.springframework.stereotype.Component;

@Component
public class AvroMapper {

    public LinkUpdate mappLinkUpdateEventToLinkUpdate(LinkUpdateEvent linkUpdateEvent) {
        return new LinkUpdate(
                linkUpdateEvent.getId(),
                linkUpdateEvent.getUrl().toString(),
                linkUpdateEvent.getDescription().toString(),
                linkUpdateEvent.getTgChatIds());
    }
}
