package backend.academy.linktracker.scrapper.client.inner;

import backend.academy.linktracker.scrapper.domain.MessageStatus;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import java.util.concurrent.CompletableFuture;

public interface BotClient<T extends UpdateEvent> {

    CompletableFuture<MessageStatus> sendUpdate(T updateEvent);
}
