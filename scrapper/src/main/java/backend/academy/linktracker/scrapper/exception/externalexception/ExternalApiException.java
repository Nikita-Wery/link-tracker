package backend.academy.linktracker.scrapper.exception.externalexception;

import java.net.URI;
import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {

    private final int statusCode;
    private final String body;
    private final URI url;

    public ExternalApiException(String message, String body, int statusCode, URI url) {
        super(message);
        this.body = body;
        this.url = url;
        this.statusCode = statusCode;
    }
}
