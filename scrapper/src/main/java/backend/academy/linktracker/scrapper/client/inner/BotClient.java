package backend.academy.linktracker.scrapper.client.inner;

import backend.academy.linktracker.scrapper.dto.UpdateEvent;

public interface BotClient<T extends UpdateEvent> {

    void sendUpdate(T updateEvent);
}
