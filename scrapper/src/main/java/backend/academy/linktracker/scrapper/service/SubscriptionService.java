package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.proto.ListLinksResponse;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import java.util.List;

public interface SubscriptionService {

    ChatLink trackLink(ChatLink chatLink);

    ChatLink untrackLink(ChatLink chatLink);

    List<ChatLink> getTrackedLinksByChatId(Long chatId);

    List<LinkResponse> getLinkResponsesByChatId(Long chatId);

    ListLinksResponse getProtoListLinksResponseByChatId(long chatId);

    LinkResponse trackLinkReturnLinkResponse(ChatLink chatLink);

    LinkResponse untrackLinkReturnLinkResponse(ChatLink chatLink);

    backend.academy.linktracker.proto.LinkResponse trackLinkReturnProtoLinkResponse(ChatLink chatLink);

    backend.academy.linktracker.proto.LinkResponse untrackLinkReturnProtoLinkResponse(ChatLink chatLink);
}
