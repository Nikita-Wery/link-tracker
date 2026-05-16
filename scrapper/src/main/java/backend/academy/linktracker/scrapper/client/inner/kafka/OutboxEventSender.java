package backend.academy.linktracker.scrapper.client.inner.kafka;

import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;

public interface OutboxEventSender<T> {

    T deserialize(String json);

    Long extractEventId(T event);

    String extractUrl(T event);

    CompletableFuture<SendResult<Long, T>> send(Long key, T event);
}
