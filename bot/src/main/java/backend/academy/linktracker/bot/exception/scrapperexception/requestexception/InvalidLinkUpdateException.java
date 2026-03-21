package backend.academy.linktracker.bot.exception.scrapperexception.requestexception;

import org.springframework.http.converter.HttpMessageNotReadableException;

public class InvalidLinkUpdateException extends HttpMessageNotReadableException {

    public InvalidLinkUpdateException(HttpMessageNotReadableException ex) {
        super(ex.getMessage(), ex.getHttpInputMessage());
    }
}
