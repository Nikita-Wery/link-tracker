package backend.academy.linktracker.bot.utils;

import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {

    public LinkUpdate mapLinkUpdateFromProto(backend.academy.linktracker.proto.LinkUpdate linkUpdateProto) {
        return new LinkUpdate(
                linkUpdateProto.getId(),
                URI.create(linkUpdateProto.getUrl()),
                linkUpdateProto.getDescription(),
                new HashSet<>(linkUpdateProto.getTgChatIdsList()));
    }

    public LinkResponse mapLinkResponseFromProto(backend.academy.linktracker.proto.LinkResponse linkResponseProto) {
        return new LinkResponse(
                linkResponseProto.getId(),
                URI.create(linkResponseProto.getUrl()),
                linkResponseProto.getTagsList(),
                Collections.emptyList());
    }

    public ListLinksResponse mapListLinksResponseFromProto(
            backend.academy.linktracker.proto.ListLinksResponse linksResponseProto) {
        return new ListLinksResponse(
                linksResponseProto.getLinksList().stream()
                        .map(this::mapLinkResponseFromProto)
                        .toList(),
                linksResponseProto.getSize());
    }
}
