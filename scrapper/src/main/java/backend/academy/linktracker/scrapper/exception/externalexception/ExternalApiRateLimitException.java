package backend.academy.linktracker.scrapper.exception.externalexception;

import java.net.URI;

public class ExternalApiRateLimitException extends ExternalApiException {

    public ExternalApiRateLimitException(String message, String body, int statusCode, URI url) {
        super(message, body, statusCode, url);
    }
}
