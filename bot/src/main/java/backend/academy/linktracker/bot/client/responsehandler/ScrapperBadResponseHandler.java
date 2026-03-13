package backend.academy.linktracker.bot.client.responsehandler;

import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ScrapperServerException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.UnknownScrapperApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

// TODO
@Slf4j
@Component
public class ScrapperBadResponseHandler {

    private final Map<String, ScrapperApiException> clientExceptionMap;
    private final ObjectMapper mapper;

    public ScrapperBadResponseHandler(
        List<ScrapperApiException> responseExceptions,
                ObjectMapper mapper) {
        this.clientExceptionMap = responseExceptions.stream()
            .collect(Collectors.toMap(
                ex -> ex.getClass().getSimpleName(), Function.identity()));
        this.mapper = mapper;
    }

    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

        int status = response.getStatusCode().value();

        switch (status / 100) {
            case 4 -> {
                ApiErrorResponse responseBody = mapper
                    .readValue(response.getBody(), ApiErrorResponse.class);

                log.error("Client error received",
                    kv("status_code", responseBody.code()),
                    kv("exception_name", responseBody.exceptionName()),
                    kv("exception_message", responseBody.exceptionMessage()),
                    kv("stacktrace", responseBody.stackTrace())
                );

                throw getExceptionByApiErrorResponseExName(responseBody);
            }
            case 5 -> {
                log.error("Server error received",
                    kv("status_code", status),
                    kv("method", request.getMethod()),
                    kv("url", request.getURI())
                );

                throw new ScrapperServerException("Bot exception 5xx");
            }
        }
    }

    private ScrapperApiException getExceptionByApiErrorResponseExName(ApiErrorResponse response) {
        return clientExceptionMap.getOrDefault(response.exceptionName(),
            new UnknownScrapperApiException("Unknown 4xx bot exception"));
    }
}
