package backend.academy.linktracker.bot.configuration.clientconfiguration;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.client.responsehandler.ScrapperBadResponseHandler;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfiguration {

    @Bean
    public ScrapperClient getScrapperClient(ScrapperBadResponseHandler handler, ScrapperProperties properties) {

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getHost())
                .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
                .build();

        return restClientFactoryMethod(restClient, ScrapperClient.class);
    }

    private <T> T restClientFactoryMethod(RestClient restClient, Class<T> clientType) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(clientType);
    }
}
