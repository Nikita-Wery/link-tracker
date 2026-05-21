package backend.academy.linktracker.scrapper.repository.cache;

import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.properties.RedisCacheProperties;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class ChatLinkRedisCache {

    private static final String CHAT_LINK_PREFIX = "chat_links:";
    private final RedisTemplate<String, LinkResponse> redisTemplate;
    private final RedisCacheProperties props;
    private final ObjectMapper objectMapper;

    private String key(Long chatId) {
        return CHAT_LINK_PREFIX + chatId;
    }

    public void put(Long chatId, LinkResponse response) {

        String redisKey = key(chatId);

        redisTemplate.opsForHash().put(redisKey, response.id().toString(), response);

        redisTemplate.expire(redisKey, props.getTtl());

        log.info("LinkResponse was successfully added, redis key: {}, linkId: {}", redisKey, response.id());
    }

    public void putAll(Long chatId, List<LinkResponse> responses) {

        if (responses.isEmpty()) {
            return;
        }

        String redisKey = key(chatId);

        Map<String, LinkResponse> map =
                responses.stream().collect(Collectors.toMap(l -> l.id().toString(), Function.identity()));

        redisTemplate.opsForHash().putAll(redisKey, map);

        redisTemplate.expire(redisKey, props.getTtl());

        log.info("Batch of linkResponses was successfully added, redis key: {}", redisKey);
    }

    public List<LinkResponse> getAll(Long chatId) {
        Collection<Object> values = redisTemplate.opsForHash().values(key(chatId));

        List<LinkResponse> result = values.stream()
                .map(v -> objectMapper.convertValue(v, LinkResponse.class))
                .toList();

        log.info(
                "User links was successfully received from redis cache, userId: {}, count of links: {}",
                chatId,
                result.size());

        return result;
    }

    public void evict(Long chatId, Long linkId) {

        redisTemplate.opsForHash().delete(key(chatId), linkId.toString());

        log.info("Users link was successfully deleted from redis cache, userId: {}, linkId: {}", chatId, linkId);
    }

    public void evictAll(Long chatId) {
        redisTemplate.delete(key(chatId));

        log.info("All users links was successfully deleted from redis cache, userId: {}", chatId);
    }
}
