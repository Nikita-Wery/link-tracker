package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class LinkNotTrackedException extends ScrapperApiException {

    private static final String LINK_NOT_TRACKED_EXCEPTION = "Данная ссыка не отслеживается на даный момент";

    public LinkNotTrackedException(String message) {
        super(message, LINK_NOT_TRACKED_EXCEPTION);
    }
}
