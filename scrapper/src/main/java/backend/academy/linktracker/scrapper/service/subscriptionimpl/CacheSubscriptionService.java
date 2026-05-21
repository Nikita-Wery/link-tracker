package backend.academy.linktracker.scrapper.service.subscriptionimpl;

import backend.academy.linktracker.proto.ListLinksResponse;
import backend.academy.linktracker.scrapper.client.inner.redis.ChatLinksEventPublisher;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkLocalCache;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkRedisCache;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import backend.academy.linktracker.scrapper.utils.GrpcMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public class CacheSubscriptionService implements SubscriptionService {

    private final SubscriptionServiceBase subscriptionService;

    private final ChatLinkLocalCache chatLinkLocalCache;
    private final ChatLinkRedisCache chatLinkRedisCache;
    private final ChatLinksEventPublisher publisher;
    private final DtoEntityMapper dtoEntityMapper;
    private final GrpcMapper grpcMapper;

    @Override
    public ChatLink trackLink(ChatLink chatLink) {

        ChatLink savedChatLink = subscriptionService.trackLink(chatLink);

        LinkResponse linkResponse = new LinkResponse(
                savedChatLink.getChatLinkId(),
                savedChatLink.getLink().getUrl(),
                savedChatLink.getTags().stream().toList());

        chatLinkRedisCache.put(savedChatLink.getChatId(), linkResponse);

        publisher.publishInvalidate(savedChatLink.getChatId());

        return savedChatLink;
    }

    @Override
    public ChatLink untrackLink(ChatLink chatLink) {

        ChatLink removedChatLink = subscriptionService.untrackLink(chatLink);

        chatLinkRedisCache.evict(removedChatLink.getChatId(), removedChatLink.getChatLinkId());

        publisher.publishInvalidate(removedChatLink.getChatId());

        return removedChatLink;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatLink> getTrackedLinksByChatId(Long chatId) {
        return subscriptionService.getTrackedLinksByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LinkResponse> getLinkResponsesByChatId(Long chatId) {

        Optional<backend.academy.linktracker.scrapper.dto.bot.ListLinksResponse> cachedLocalLinks =
                chatLinkLocalCache.get(chatId);

        if (cachedLocalLinks.isPresent()) {
            return cachedLocalLinks.orElseThrow().links();
        }

        List<LinkResponse> cachedRedisLinks = chatLinkRedisCache.getAll(chatId);

        if (!cachedRedisLinks.isEmpty()) {
            chatLinkLocalCache.put(
                    chatId,
                    new backend.academy.linktracker.scrapper.dto.bot.ListLinksResponse(
                            cachedRedisLinks, cachedRedisLinks.size()));

            return cachedRedisLinks;
        }

        List<LinkResponse> linkResponses = getTrackedLinksByChatId(chatId).stream()
                .map(dtoEntityMapper::linkToLinkResponse)
                .toList();

        chatLinkLocalCache.put(
                chatId,
                new backend.academy.linktracker.scrapper.dto.bot.ListLinksResponse(
                        linkResponses, linkResponses.size()));
        chatLinkRedisCache.putAll(chatId, linkResponses);

        return linkResponses;
    }

    @Override
    @Transactional(readOnly = true)
    public ListLinksResponse getProtoListLinksResponseByChatId(long chatId) {
        return grpcMapper.listOfLinkResponsesToProto(getLinkResponsesByChatId(chatId));
    }

    @Override
    public LinkResponse trackLinkReturnLinkResponse(ChatLink chatLink) {
        return dtoEntityMapper.linkToLinkResponse(trackLink(chatLink));
    }

    @Override
    public LinkResponse untrackLinkReturnLinkResponse(ChatLink chatLink) {

        ChatLink removedChatLink = untrackLink(chatLink);

        return new LinkResponse(
                removedChatLink.getChatLinkId(),
                removedChatLink.getLink().getUrl(),
                removedChatLink.getTags().stream().toList());
    }
}
