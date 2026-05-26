package backend.academy.linktracker.scrapper.client.responsehandler;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiClientException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiRateLimitException;
import backend.academy.linktracker.scrapper.exception.externalexception.ExternalApiServerException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class APIBadResponseHandler {

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

        int statusCode = response.getStatusCode().value();

        String body;
        try (InputStream is = response.getBody()) {
            body = is != null ? new String(is.readAllBytes(), StandardCharsets.UTF_8) : "";
        }

        log.error(
                "External API error",
                kv("status_code", statusCode),
                kv("response_body", body),
                kv("method", request.getMethod().name()),
                kv("uri", request.getURI().toString()));

        if (statusCode == 429) {
            throw new ExternalApiRateLimitException(
                    "Too many request external api error", body, statusCode, request.getURI());
        }

        if (statusCode >= 500) {
            throw new ExternalApiServerException("External api server error", body, statusCode, request.getURI());
        }

        if (statusCode >= 400) {
            throw new ExternalApiClientException("External api client error", body, statusCode, request.getURI());
        }

        throw new ExternalApiException("Unknown external api error", body, statusCode, request.getURI());
    }
}
