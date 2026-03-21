package backend.academy.linktracker.scrapper.client.inner;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface RestBotClient {

    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdate update);
}
