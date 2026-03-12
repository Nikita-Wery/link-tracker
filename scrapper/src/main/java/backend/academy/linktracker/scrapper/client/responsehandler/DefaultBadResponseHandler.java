package backend.academy.linktracker.scrapper.client.responsehandler;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpRequest;
import java.io.IOException;

public interface DefaultBadResponseHandler {

    void handle(HttpRequest request, ClientHttpResponse response) throws IOException;

}
