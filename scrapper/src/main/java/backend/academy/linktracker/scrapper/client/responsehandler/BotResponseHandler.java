package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.client.responsehandler.statuscodepolicy.RetryPolicy;
import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.NonRetryableHttpException;
import backend.academy.linktracker.scrapper.exception.RetryableHttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class BotResponseHandler implements DefaultResponseHandler {

    private final RetryPolicy retryPolicy;

    private final ObjectMapper objectMapper;

    public BotResponseHandler(RetryPolicy retryPolicy, ObjectMapper objectMapper) {
        this.retryPolicy = retryPolicy;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

        int status = response.getStatusCode().value();

        switch (status / 100) {

            case 4 -> {
                ApiErrorResponse error = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);

                log.error("Client error received",
                    kv("status_code", error.code()),
                    kv("exception_name", error.exceptionName()),
                    kv("exception_message", error.exceptionMessage()),
                    kv("stacktrace", error.stackTrace())
                );

                throw new NonRetryableHttpException(
                    status, "Client error: " + status
                );
            }
            case 5 -> {
                log.error("Server error received",
                    kv("status_code", status),
                    kv("method", request.getMethod()),
                    kv("url", request.getURI())
                );

                if (retryPolicy.shouldRetry(status)) {
                    throw new RetryableHttpException(status, "Retryable server error");
                }
                throw new NonRetryableHttpException(status, "Server error");
            }

            default -> {
                log.error("Unexpected response code",
                    kv("status_code", status),
                    kv("method", request.getMethod()),
                    kv("url", request.getURI())
                );

                if (retryPolicy.shouldRetry(status)) {
                    throw new RetryableHttpException(status, "Retryable status");
                }
            }
        }
    }

}
