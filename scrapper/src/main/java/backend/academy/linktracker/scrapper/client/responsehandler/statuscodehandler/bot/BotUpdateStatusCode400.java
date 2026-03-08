package backend.academy.linktracker.scrapper.client.responsehandler.statuscodehandler.bot;

import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.ParseResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class BotUpdateStatusCode400 extends BotStatusCodeDefaultHandler {

    private final ObjectMapper objectMapper;

    public BotUpdateStatusCode400(ObjectMapper objectMapper) {
        super(Set.of(400));
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) {

        try {
            ApiErrorResponse errorResponse = objectMapper.readValue(
                response.getBody(),
                ApiErrorResponse.class
            );

            log.error("Client error occurred",
                kv("code", errorResponse.code()),
                kv("exception_name", errorResponse.exceptionName()),
                kv("exception_message", errorResponse.exceptionMessage()),
                kv("stacktrace", errorResponse.stackTrace())
            );
        } catch (IOException ex) {

            log.error("Failed to parse API error response",
                kv("target_type", ApiErrorResponse.class.getSimpleName()),
                kv("method", request.getMethod()),
                kv("request_uri", request.getURI()),
                kv("exception", ex.getClass().getSimpleName()),
                kv("exception_message", ex.getMessage())
            );

            throw new ParseResponseException("Failed to parse %s".formatted(ApiErrorResponse.class),ex);

        }

    }
}
