package backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest;

import backend.academy.linktracker.scrapper.client.inner.RestBotClient;
import backend.academy.linktracker.scrapper.client.responsehandler.BotBadResponseHandler;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.conditional.RestApiConditional;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@Configuration
@Conditional(RestApiConditional.class)
public class RestBotClientConfiguration {

    @Bean
    public BotBadResponseHandler botBadResponseHandler(
            ObjectMapper objectMapper, List<BotApiException> responseExceptions) {
        return new BotBadResponseHandler(objectMapper, responseExceptions);
    }

    @Bean
    public RestBotClient botClient(BotBadResponseHandler handler, BotProperties properties) {

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getHost())
                .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
                .build();

        return restClientFactoryMethod(restClient, RestBotClient.class);
    }

    private <T> T restClientFactoryMethod(RestClient restClient, Class<T> clientType) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(clientType);
    }
}
