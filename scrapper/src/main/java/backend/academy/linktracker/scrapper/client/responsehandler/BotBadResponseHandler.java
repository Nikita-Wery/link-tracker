package backend.academy.linktracker.scrapper.client.responsehandler;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;
import backend.academy.linktracker.scrapper.exception.botexception.responsexception.BotServerException;
import backend.academy.linktracker.scrapper.exception.botexception.responsexception.UnknownBotException;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import tools.jackson.databind.ObjectMapper;

@Slf4j
public class BotBadResponseHandler {

    private final Map<String, BotApiException> clientExceptionMap;
    private final ObjectMapper objectMapper;
    private final ScrapperMetricsService scrapperMetricsService;

    public BotBadResponseHandler(
            ObjectMapper objectMapper,
            List<BotApiException> responseExceptions,
            ScrapperMetricsService scrapperMetricsService) {

        this.clientExceptionMap = responseExceptions.stream()
                .collect(Collectors.toMap(ex -> ex.getClass().getSimpleName(), Function.identity()));

        this.objectMapper = objectMapper;
        this.scrapperMetricsService = scrapperMetricsService;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

        int status = response.getStatusCode().value();

        switch (status / 100) {
            case 4 -> {
                ApiErrorResponse responseBody = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);

                log.error(
                        "Client error received",
                        kv("status_code", responseBody.code()),
                        kv("exception_name", responseBody.exceptionName()),
                        kv("exception_message", responseBody.exceptionMessage()),
                        kv("stacktrace", responseBody.stackTrace()));

                throw getExceptionByApiErrorResponseExName(responseBody);
            }
            case 5 -> {
                log.error(
                        "Server error received",
                        kv("status_code", status),
                        kv("method", request.getMethod()),
                        kv("url", request.getURI()));

                scrapperMetricsService.incrementApiError(request.getURI().getPath());

                throw new BotServerException("Bot exception 5xx");
            }
        }
    }

    private BotApiException getExceptionByApiErrorResponseExName(ApiErrorResponse response) {
        return clientExceptionMap.getOrDefault(
                response.exceptionName(), new UnknownBotException("Unknown 4xx bot exception"));
    }
}
