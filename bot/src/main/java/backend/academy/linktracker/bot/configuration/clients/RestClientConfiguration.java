package backend.academy.linktracker.bot.configuration.clients;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.responsehandler.RestScrapperBadResponseHandler;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
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
@ConditionalOnProperty(name = "app.client.scrapper.api.rest.enabled", havingValue = "true")
public class RestClientConfiguration {

    @Bean
    public RestScrapperBadResponseHandler restScrapperBadResponseHandler(
            List<ScrapperApiException> exceptionList, ObjectMapper mapper) {
        return new RestScrapperBadResponseHandler(exceptionList, mapper);
    }

    @Bean
    public ScrapperClient getScrapperClient(
            RestScrapperBadResponseHandler handler, ScrapperProperties properties, ClientHttpRequestFactory factory) {

        RestClient restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(properties.getHost())
                .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
                .build();

        return restClientFactoryMethod(restClient, ScrapperClient.class);
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(ScrapperProperties properties) {

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
