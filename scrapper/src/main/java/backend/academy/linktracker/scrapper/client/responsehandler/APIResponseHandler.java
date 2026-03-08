package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy.RetryPolicy;
import backend.academy.linktracker.scrapper.exception.NonRetryableHttpException;
import backend.academy.linktracker.scrapper.exception.RetryableHttpException;
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
public class APIResponseHandler implements DefaultResponseHandler {

    private final RetryPolicy retryPolicy;

    public APIResponseHandler(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

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

        if (retryPolicy.shouldRetry(statusCode)) {
            throw new RetryableHttpException(statusCode, "Retryable statusCode: " + statusCode);
        }

        throw new NonRetryableHttpException(statusCode, "Non retryable statusCode: " + statusCode);
    }

}
