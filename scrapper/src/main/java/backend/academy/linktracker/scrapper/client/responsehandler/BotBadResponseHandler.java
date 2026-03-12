package backend.academy.linktracker.scrapper.client.responsehandler;

import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.exception.botexception.resposexception.BotServerException;
import backend.academy.linktracker.scrapper.exception.botexception.resposexception.UnknownBotClientException;
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

@Slf4j
@Component
public class BotBadResponseHandler implements DefaultBadResponseHandler {

    private final Map<String, BotApiException> clientExceptionMap;
    private final ObjectMapper objectMapper;

    public BotBadResponseHandler(
            ObjectMapper objectMapper,
            List<BotApiException> responseExceptions) {

        this.clientExceptionMap = responseExceptions.stream()
                .collect(Collectors.toMap(
                    ex -> ex.getClass().getSimpleName(), Function.identity()));

        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

        int status = response.getStatusCode().value();

        switch (status / 100) {
            case 4 -> {
                ApiErrorResponse responseBody = objectMapper
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

                throw new BotServerException("Bot exception 5xx");
            }
        }
    }

    private BotApiException getExceptionByApiErrorResponseExName(ApiErrorResponse response) {
        return clientExceptionMap.getOrDefault(response.exceptionName(),
                new UnknownBotClientException("Unknown 4xx bot exception"));
    }

}
