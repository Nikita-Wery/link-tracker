package backend.academy.linktracker.scrapper.utils;

import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class JsonToEntityDeserializer {

    private final ObjectMapper objectMapper;

    public LinkUpdateEvent deserializeLinkUpdateEvent(String json) {
        LinkUpdate linkUpdate = objectMapper.convertValue(json, LinkUpdate.class);

        return LinkUpdateEvent.newBuilder()
                .setId(linkUpdate.id())
                .setUrl(linkUpdate.url().toString())
                .setDescription(linkUpdate.description())
                .setTgChatIds(linkUpdate.tgChatIds().stream().toList())
                .build();
    }

    public LinkUpdate deserializeLinkUpdate(String json) {
        return objectMapper.convertValue(json, LinkUpdate.class);
    }
}
