package backend.academy.linktracker.scrapper.service.eventhandler;

import backend.academy.linktracker.scrapper.client.inner.RestBotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LinkUpdateHandler {

    private final RestBotClient botClient;

    public LinkUpdateHandler(RestBotClient botClient) {
        this.botClient = botClient;
    }

    @EventListener
    public void handle(LinkUpdate event) {
        botClient.sendUpdate(event);
    }
}
