package backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public interface StatusCodeDefaultHandler {

    boolean supportsCode(int code);

    void handle(HttpRequest request, ClientHttpResponse response);

}
