package backend.academy.linktracker.scrapper.config.scrapperconfiguration;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.client.responsehandler.APIResponseHandler;
import backend.academy.linktracker.scrapper.client.responsehandler.BotResponseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrapperConfiguration {

    @Bean
    public BotClient botClient(RestClientFactory factory, APIResponseHandler handler) {

        // TODO: убрать hardcode и подтягивать из property class
        return factory.createClient(
            "http://localhost:8080",
                handler,
                BotClient.class
        );
    }

    @Bean
    public GitHubClient gitHubClient(RestClientFactory factory, APIResponseHandler handler) {

        // TODO: убрать hardcode
        return factory.createClient(
            "https://api.github.com",
            handler,
            GitHubClient.class
        );
    }

    @Bean
    public StackOverflowClient stackOverflowClient(RestClientFactory factory, BotResponseHandler handler) {

        // TODO: убрать hardcode
        return factory.createClient(
            "https://api.stackexchange.com",
            handler,
            StackOverflowClient.class
        );
    }

}
