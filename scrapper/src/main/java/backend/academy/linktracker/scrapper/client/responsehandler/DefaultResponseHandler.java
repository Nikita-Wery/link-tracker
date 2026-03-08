package backend.academy.linktracker.scrapper.client.responsehandler;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpRequest;

public interface DefaultResponseHandler {

    void handle(HttpRequest request, ClientHttpResponse response);

}
