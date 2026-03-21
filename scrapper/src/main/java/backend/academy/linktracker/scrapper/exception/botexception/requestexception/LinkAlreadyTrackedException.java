package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class LinkAlreadyTrackedException extends ScrapperApiException {

    private static final String LINK_ALREADY_TRACKED_EXCEPTION = "Данная ссылка уже отслеживается пользователем";

    public LinkAlreadyTrackedException(String message) {
        super(message, LINK_ALREADY_TRACKED_EXCEPTION);
    }
}
