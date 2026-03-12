package backend.academy.linktracker.scrapper.exception.externalexception;

import lombok.Getter;
import java.net.URI;

@Getter
public class ExternalApiException extends RuntimeException {

    private final int statusCode;
    private final URI url;

    public ExternalApiException(String message, int statusCode, URI url) {
        super(message);
        this.url = url;
        this.statusCode = statusCode;
    }
}
