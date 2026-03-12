package backend.academy.linktracker.scrapper.config.scrapperconfiguration;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.responsehandler.APIBadResponseHandler;
import backend.academy.linktracker.scrapper.client.responsehandler.BotBadResponseHandler;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ScrapperConfiguration {

    @Bean
    public BotClient botClient(
        BotBadResponseHandler handler,
        BotProperties properties) {

        RestClient restClient = RestClient.builder()
            .baseUrl(properties.getHost())
            .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
            .build();

        return restClientFactoryMethod(restClient, BotClient.class);
    }

    @Bean
    public GitHubClient gitHubClient(
        APIBadResponseHandler handler,
        GithubProperties properties) {

        RestClient restClient = RestClient.builder()
            .baseUrl(properties.getHost())
            .defaultHeader("Authorization", "Bearer" + properties.getToken())
            .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
            .build();

        return restClientFactoryMethod(restClient, GitHubClient.class);
    }

    @Bean
    public StackOverflowClient stackOverflowClient(
        APIBadResponseHandler handler,
        StackoverflowProperties properties) {

        RestClient restClient = RestClient.builder()
            .baseUrl(properties.getHost())
            .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
            .build();

        return restClientFactoryMethod(restClient, StackOverflowClient.class);
    }

    private <T> T restClientFactoryMethod(RestClient restClient, Class<T> clientType) {
        HttpServiceProxyFactory factory =
            HttpServiceProxyFactory.builderFor(
                RestClientAdapter.create(restClient)
            ).build();

        return factory.createClient(clientType);
    }

}
