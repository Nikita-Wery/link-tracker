package backend.academy.linktracker.scrapper.exception.externalexception;

import java.net.URI;

public class ExternalApiServerException extends ExternalApiException {

    public ExternalApiServerException(String message, String body, int statusCode, URI url) {
        super(message, body, statusCode, url);
    }
}
