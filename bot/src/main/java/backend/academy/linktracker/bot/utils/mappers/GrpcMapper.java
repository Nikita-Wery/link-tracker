package backend.academy.linktracker.bot.utils.mappers;

import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.LinkUpdate;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import java.net.URI;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {

    public LinkUpdate mapLinkUpdateFromProto(backend.academy.linktracker.proto.LinkUpdate linkUpdateProto) {
        return new LinkUpdate(
                linkUpdateProto.getId(),
                linkUpdateProto.getUrl(),
                linkUpdateProto.getDescription(),
                new ArrayList<>(linkUpdateProto.getTgChatIdsList()));
    }

    public LinkResponse mapLinkResponseFromProto(backend.academy.linktracker.proto.LinkResponse linkResponseProto) {
        return new LinkResponse(
                linkResponseProto.getId(), URI.create(linkResponseProto.getUrl()), linkResponseProto.getTagsList());
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
