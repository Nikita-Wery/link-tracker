package backend.academy.linktracker.bot.client.responsehandler;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.bot.exception.ScrapperApiException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.UnknownScrapperApiException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcScrapperBadResponseHandler {

    private static final Metadata.Key<String> EXCEPTION_NAME =
            Metadata.Key.of("exception_name", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> EXCEPTION_MESSAGE =
            Metadata.Key.of("exception_message", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> DESCRIPTION =
            Metadata.Key.of("description", Metadata.ASCII_STRING_MARSHALLER);

    private final Map<String, ScrapperApiException> scrapperExceptionsMap;

    public GrpcScrapperBadResponseHandler(List<ScrapperApiException> exceptionList) {
        scrapperExceptionsMap = new HashMap<>();

        for (ScrapperApiException ex : exceptionList) {
            scrapperExceptionsMap.put(ex.getClass().getSimpleName(), ex);
        }
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public RuntimeException handle(StatusRuntimeException e) {
        Metadata trailers = e.getTrailers();
        String exceptionName = trailers != null ? trailers.get(EXCEPTION_NAME) : null;
        String exceptionMessage = trailers != null ? trailers.get(EXCEPTION_MESSAGE) : null;
        String description = trailers != null ? trailers.get(DESCRIPTION) : null;

        log.error(
                "Grpc client error received",
                kv("status_code", e.getStatus().getCode()),
                kv("exception_name", exceptionName),
                kv("exception_message", exceptionMessage),
                e);

        String exDescription = description != null ? description : e.getStatus().getDescription();

        throw getExceptionByApiErrorResponseExName(exceptionName, exDescription);
    }

    private ScrapperApiException getExceptionByApiErrorResponseExName(String exceptionName, String defaultDescription) {
        return scrapperExceptionsMap.getOrDefault(exceptionName, new UnknownScrapperApiException(defaultDescription));
    }
}
