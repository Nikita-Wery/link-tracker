package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class LinkNotExistsException extends ScrapperApiException {
    public LinkNotExistsException(String message) {
        super(message);
    }
}
