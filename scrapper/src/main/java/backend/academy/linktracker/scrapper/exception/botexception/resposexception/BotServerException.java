package backend.academy.linktracker.scrapper.exception.botexception.resposexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class BotServerException extends BotApiException {
    public BotServerException(String message) {
        super(message);
    }
}
