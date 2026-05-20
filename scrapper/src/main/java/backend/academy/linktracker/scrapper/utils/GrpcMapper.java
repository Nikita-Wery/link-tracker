package backend.academy.linktracker.scrapper.utils;

import backend.academy.linktracker.proto.LinkResponse;
import backend.academy.linktracker.proto.ListLinksResponse;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GrpcMapper {

    public LinkResponse linkToGrpcLinkResponse(ChatLink chatLink) {
        return LinkResponse.newBuilder()
                .setId(chatLink.getChatLinkId())
                .setUrl(chatLink.getLink().getUrl())
                .addAllTags(chatLink.getTags())
                .build();
    }

    public ListLinksResponse listOfLinksToLinksResponse(List<ChatLink> links) {
        List<LinkResponse> linkResponses =
                links.stream().map(this::linkToGrpcLinkResponse).toList();

        return ListLinksResponse.newBuilder()
                .addAllLinks(linkResponses)
                .setSize(linkResponses.size())
                .build();
    }

    public AddLinkRequest mapAddLinkRequest(backend.academy.linktracker.proto.AddLinkRequest req) {
        return new AddLinkRequest(req.getUrl(), req.getTagsList().stream().toList());
    }

    public RemoveLinkRequest mapRemoveLinkRequest(backend.academy.linktracker.proto.RemoveLinkRequest req) {
        return new RemoveLinkRequest(req.getLink());
    }

    public ListLinksResponse listOfLinkResponsesToProto(List<backend.academy.linktracker.scrapper.dto.bot.LinkResponse> linkResponses) {
        return ListLinksResponse.newBuilder()
            .addAllLinks(linkResponses.stream()
                .map(l
                    -> LinkResponse.newBuilder()
                            .addAllTags(l.tags())
                            .setUrl(l.url())
                            .setId(l.id())
                            .build()
                ).toList())
            .setSize(linkResponses.size())
            .build();
    }
}
