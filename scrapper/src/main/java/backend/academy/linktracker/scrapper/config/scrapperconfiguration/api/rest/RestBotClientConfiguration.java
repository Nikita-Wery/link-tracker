package backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest;

import backend.academy.linktracker.scrapper.client.inner.impl.RestBotClient;
import backend.academy.linktracker.scrapper.client.responsehandler.BotBadResponseHandler;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@Configuration
@ConditionalOnProperty(name = "app.client.bot.api.rest.enabled", havingValue = "true", matchIfMissing = true)
public class RestBotClientConfiguration {

    @Bean
    public BotBadResponseHandler botBadResponseHandler(
            ObjectMapper objectMapper,
            List<BotApiException> responseExceptions,
            ScrapperMetricsService scrapperMetricsService) {
        return new BotBadResponseHandler(objectMapper, responseExceptions, scrapperMetricsService);
    }

    @Bean
    public RestBotClient botClient(
            BotBadResponseHandler handler, BotProperties properties, ClientHttpRequestFactory factory) {

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(properties.getHost())
                .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
                .build();

        return restClientFactoryMethod(restClient, RestBotClient.class);
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(BotProperties properties) {

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeout().getConnection());
        factory.setReadTimeout(properties.getTimeout().getRead());

        return factory;
    }

    private <T> T restClientFactoryMethod(RestClient restClient, Class<T> clientType) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(clientType);
    }
}
