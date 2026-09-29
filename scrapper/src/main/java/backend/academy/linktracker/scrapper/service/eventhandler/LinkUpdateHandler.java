package backend.academy.linktracker.scrapper.service.eventhandler;

import backend.academy.linktracker.scrapper.client.inner.BotClient;
import backend.academy.linktracker.scrapper.client.inner.impl.RestBotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.UpdateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LinkUpdateHandler {

    private final BotClient<LinkUpdate> botClient;

    public LinkUpdateHandler(RestBotClient botClient) {
        this.botClient = botClient;
    }

    @EventListener
    public void handle(LinkUpdate event) {
        botClient.sendUpdate(event);
    }
}
