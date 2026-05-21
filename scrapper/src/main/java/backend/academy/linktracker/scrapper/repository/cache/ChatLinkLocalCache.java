package backend.academy.linktracker.scrapper.repository.cache;

import backend.academy.linktracker.scrapper.dto.bot.ListLinksResponse;
import backend.academy.linktracker.scrapper.properties.LocalCacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Optional;

public class ChatLinkLocalCache {

    private final Cache<Long, ListLinksResponse> cache;

    public ChatLinkLocalCache(LocalCacheProperties properties) {

        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.getMaxSize())
                .expireAfterWrite(properties.getTtl())
                .build();
    }

    public Optional<ListLinksResponse> get(Long chatId) {
        return Optional.ofNullable(cache.getIfPresent(chatId));
    }

    public void put(Long chatId, ListLinksResponse value) {
        cache.put(chatId, value);
    }

    public void evict(Long chatId) {
        cache.invalidate(chatId);
    }
}
