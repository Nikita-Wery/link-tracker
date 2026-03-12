package backend.academy.linktracker.scrapper.exception.botexception.resposexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class InvalidLinkUpdate extends BotApiException {

    public InvalidLinkUpdate(String message) {
        super(message);
    }
}
