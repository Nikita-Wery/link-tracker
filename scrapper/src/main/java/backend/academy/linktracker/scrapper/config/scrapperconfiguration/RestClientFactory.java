package backend.academy.linktracker.scrapper.config.scrapperconfiguration;

import backend.academy.linktracker.scrapper.client.responsehandler.DefaultResponseHandler;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
public class RestClientFactory {

    public <T> T createClient(
            String baseUrl,
            DefaultResponseHandler handler,
            Class<T> clientType
    ) {

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, handler::handle)
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(
                        RestClientAdapter.create(restClient)
                ).build();

        return factory.createClient(clientType);
    }
}
