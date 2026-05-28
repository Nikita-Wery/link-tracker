package backend.academy.linktracker.scrapper.utils;

import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@AllArgsConstructor
public class JsonToEntityDeserializer {

    private final ObjectMapper objectMapper;
    private final Pattern pattern = Pattern.compile("AUTHOR=([^\\n\\r]+)");

    public LinkUpdateEvent deserializeLinkUpdateEvent(String json) {
        LinkUpdate linkUpdate = objectMapper.readValue(json, LinkUpdate.class);

        return LinkUpdateEvent.newBuilder()
                .setId(linkUpdate.id())
                .setUrl(linkUpdate.url().toString())
                .setDescription(linkUpdate.description())
                .setTgChatIds(linkUpdate.tgChatIds().stream().toList())
                .build();
    }

    public LinkUpdate deserializeLinkUpdate(String json) {
        return objectMapper.readValue(json, LinkUpdate.class);
    }

    public RawLinkUpdateEvent deserializeRawLinkUpdateEvent(String json) {

        LinkUpdate linkUpdate = objectMapper.readValue(json, LinkUpdate.class);

        String author = Optional.ofNullable(linkUpdate.description())
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(m -> m.group(1))
                .orElse("unknown");

        return RawLinkUpdateEvent.newBuilder()
                .setId(linkUpdate.id())
                .setAuthor(author)
                .setDescription(linkUpdate.description())
                .setTgChatIds(linkUpdate.tgChatIds().stream().toList())
                .setUrl(linkUpdate.url().toString())
                .build();
    }
}
