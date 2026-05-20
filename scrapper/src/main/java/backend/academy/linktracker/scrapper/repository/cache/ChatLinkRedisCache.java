package backend.academy.linktracker.scrapper.repository.cache;

import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.properties.RedisCacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ChatLinkRedisCache {

    private static final String CHAT_LINK_PREFIX = "chat_links:";
    private final RedisTemplate<String, LinkResponse> redisTemplate;
    private final RedisCacheProperties props;

    private String key(Long chatId) {
        return CHAT_LINK_PREFIX + chatId;
    }

    public void put(Long chatId, LinkResponse response) {

        String redisKey = key(chatId);

        redisTemplate
                .opsForHash()
                .put(redisKey,
                    response.id().toString(),
                    response);

        redisTemplate.expire(
                redisKey,
                props.getTtl()
        );
    }

    public void putAll(
            Long chatId,
            List<LinkResponse> responses) {

        if (responses.isEmpty()) {
            return;
        }

        String redisKey = key(chatId);

        Map<String, LinkResponse> map =
                responses.stream()
                        .collect(Collectors.toMap(l -> l.id().toString(), Function.identity()));

        redisTemplate
                .opsForHash()
                .putAll(redisKey, map);

        redisTemplate.expire(
                redisKey,
                props.getTtl()
        );
    }

    public List<LinkResponse> getAll(Long chatId) {

        Collection<Object> values =
            redisTemplate.opsForHash().values(key(chatId));

        return values.stream()
                .map(v -> (LinkResponse) v)
                .toList();
    }

    public void evict(
            Long chatId,
            Long linkId) {

        redisTemplate
                .opsForHash()
                .delete(key(chatId), linkId.toString());
    }

    public void evictAll(Long chatId) {
        redisTemplate.delete(key(chatId));
    }
}
