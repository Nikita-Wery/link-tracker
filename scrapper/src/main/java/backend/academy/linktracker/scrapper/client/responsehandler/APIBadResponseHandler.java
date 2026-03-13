package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class APIBadResponseHandler {

    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

        int statusCode = response.getStatusCode().value();

        String body;

        try (InputStream is = response.getBody()) {
            body = is != null
                ? new String(is.readAllBytes(), StandardCharsets.UTF_8)
                : "";
        }

        log.error("External API error",
            kv("status_code", statusCode),
            kv("response_body", body),
            kv("method", request.getMethod().name()),
            kv("uri", request.getURI().toString())
        );

        throw new ExternalApiException("External API error", statusCode, request.getURI());
    }

}
