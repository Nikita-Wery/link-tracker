package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class LinkNotTrackedException extends ScrapperApiException {
    public LinkNotTrackedException(String message) {
        super(message);
    }
}
